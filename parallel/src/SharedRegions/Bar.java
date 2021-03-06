package SharedRegions;

import Main.*;
import Entities.*;

/**
 * 
 *  General Description:
 *      Definition of the Bar Shared Region - monitor-based solution.
 * 
 *  Authors Filipe Pires (85122) and Isaac dos Anjos (78191)
 */
public class Bar {
    
    /**
     *  Internal Data
     */
    
    private GeneralRepository repo;                                             // Restaurant's Repository
    private volatile char alternative;                                          // tells the Waiter what to do
                            /**
                             * 's' means salute student
                             * 'o' means take table order
                             * 'p' means wait for portions
                             * 'b' means process table bill
                             * 'g' means say goodbye
                             * 'e' means end
                             */
    private volatile int arrivedStudents,                                       // aux var to know in which place did each Student arrive
                         students_on_hold,                                      // how many students are on hold
                         signalled_once;                                        // guarantee that signal the waiter goes off once
    
    /**
     *  Constructor
     *  Allocates a new Bar Shared Region.
     * 
     *  @param repo GeneralRepository object for the entities states' to be monitored through a file log.
     */
    public Bar(GeneralRepository repo){
        this.repo = repo;
        alternative = '\0';
        students_on_hold=0;
        signalled_once = 0;
    }
    
    /**
     *  Bar's Methods
     */
    
    /**
     *  Used by Waiter to return his current alternative (action to be done) or empty if there is nothing to be done.
     * 
     *  @return Returns: '\0' if Waiter has nothing to do; 's' if there is a Student to salute; 'o' if there is an order to take; 'p' if Waiter needs to wait for a portion; 'b' if there is a bill to process; 'g' if there is a Student to say goodbye; 'e' if all activities have been executed.
     */
    public synchronized char lookAround() {
        if (((Waiter)Thread.currentThread()).setWaiterState(Waiter.WaiterState.AS) ) {
            repo.updateWaiterState(((Waiter)Thread.currentThread()).getWaiterState());
        }
        
        if(repo.haveAllStudentLeft() && alternative != 'e')
            alternative='g';                                                    //Waiter says goodbye to everyone
        
        if(students_on_hold>0){
            this.notifyAll();
           
            students_on_hold=0;
        }
        while(alternative == '\0' ) {
            try {
                this.wait();                                                    // Waiter blocks while nothing happens
            } catch (InterruptedException ie) {}
        }
       
        return alternative;
    }
    
    /**
     *  Used by Student to inform the Waiter of his arrival.
     * 
     *  @return Returns the Student's place of arrival (variable type = ArrivalOrder).
     */
    public synchronized TheRestaurantMain.ArrivalOrder enter() {
        while(alternative != '\0') {
            try {
                this.wait();                                                    // Student blocks if and while Waiter is occupied (saluting another Student)
            } catch (InterruptedException ie) {}
        }
        
        if (((Student) Thread.currentThread()).setStudentState(Student.StudentState.TASATT)) {
            repo.updateStudentState(((Student) Thread.currentThread()).getStudentState(), ((Student) Thread.currentThread()).getID());
        }
        
        TheRestaurantMain.ArrivalOrder arrivedIn;                               // tells a Student in which place did he arrive
        arrivedStudents++;
        if(arrivedStudents==1) {
            arrivedIn = TheRestaurantMain.ArrivalOrder.FIRST;
        } else if(arrivedStudents==TheRestaurantMain.nstudents) {
            arrivedIn = TheRestaurantMain.ArrivalOrder.LAST;
        } else {
            arrivedIn = TheRestaurantMain.ArrivalOrder.MIDDLE;
           
        }
        this.notifyAll();
        while(alternative != '\0' ) {
            try {
                students_on_hold++;
                this.wait();                                                    // Student blocks if and while Waiter is occupied (saluting other Student)
            } catch (InterruptedException ie) {}
        }
        
        alternative = 's';
        this.notifyAll();                                                       // Student notifies the Waiter of his arrival
        
        return arrivedIn;
    }
    
    /**
     *  Used by Waiter to return to the Bar and reset the alternative (action to be done).
     */
    public synchronized void returnToTheBar() { 
        if (((Waiter)Thread.currentThread()).setWaiterState(Waiter.WaiterState.AS) ) {
            repo.updateWaiterState(((Waiter)Thread.currentThread()).getWaiterState());
        }
        
        alternative = '\0';
        this.notifyAll();                                                       // Waiter notifies anyone that is waiting for him
    }
    
    /**
     *  Used by Student to notify the Waiter once every Student has chosen their courses.
     */
    public synchronized void callTheWaiter() {
        alternative = 'o';
        this.notifyAll();                                                       // First Student calls the Waiter once everyone has chosen their courses
    }
    
    /**
     *  Used by Chef to notify the Waiter everytime he finishes dishing a portion.
     */
    public synchronized void alertTheWaiter() {
        if (((Chef)Thread.currentThread()).setChefState(Chef.ChefState.DeTP) ) {
            repo.updateChefState(((Chef)Thread.currentThread()).getChefState());
        }
        
        while(alternative != '\0'  && alternative !='p') {
            try {
                this.wait();                                                    // Chef blocks if and while Waiter is occupied (delivering another portion at the Table)
            } catch (InterruptedException ie) {}
        }
        
        alternative = 'p';
        this.notifyAll();                                                       // Chef notifies the Waiter that the course is ready to be delivered
    }
    
    /**
     *  Used by Student to notify the Waiter once he is ready to pay the bill.
     */
    public synchronized void signalTheWaiter() {
        if(signalled_once>0) {
            return;
        }
        signalled_once++;
        
        while(alternative != '\0')
            try{wait();}catch(Exception e){}                                    // Student blocks if and while Waiter is occupied
        
        alternative = 'b';
        this.notifyAll();                                                       // Student signals the Waiter to pay the bill
    }
    
    /**
     *  Used by Waiter to prepare the Table's bill.
     */
    public synchronized void prepareTheBill() {
        if (((Waiter)Thread.currentThread()).setWaiterState(Waiter.WaiterState.PTB) ) {
            repo.updateWaiterState(((Waiter)Thread.currentThread()).getWaiterState());
        }
    }
    
    /**
     *  Used by Waiter to say goodbye to each and every Student.
     */
    public synchronized void sayGoodbye() {
        alternative='e';
    }
    
    /**
     *  Auxiliar Methods
     */
    
    /**
     *  Getter function that serves as an auxiliar for the entities. 
     * 
     *  @return Returns the Waiter's current alternative (activity to be done).
     */
    public char getAlternative() { return alternative; }
    
    /**
     *  Sets a new alternative for the Waiter.
     * 
     *  @param alternative New alternative for the Waiter.
     */
    public void setAlternative(char alternative) { this.alternative = alternative; }
}
