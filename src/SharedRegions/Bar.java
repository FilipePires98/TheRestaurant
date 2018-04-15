package SharedRegions;
import Main.*;
import Entities.*;
import genclass.GenericIO;

/**
 * 
 *  General Description:
 *      Definition of the Bar Shared Region - monitor-based solution.
 * 
 *  @authors Filipe Pires (85122) & Isaac dos Anjos (78191)
 */
public class Bar {
    
    /**
     *  Internal Data
     */
    
    private GeneralRepository repo;                                             // Restaurant's Repository
    private char alternative;                                                   // tells the Waiter what to do
                            /**
                             * 's' means salute student
                             * 'o' means take table order
                             * 'p' means wait for portions
                             * 'b' means process table bill
                             * 'g' means say goodbye
                             * 'e' means end
                             */
    
    /**
     *  Constructor
     *  Allocates a new Bar Shared Region.
     *  @param waiter Waiter Thread responsible for the Restaurant's Bar
     */
    public Bar(GeneralRepository repo){
        this.repo = repo;
        alternative = '\0';
    }
    
    /**
     *  Bar's Methods
     */
    
    /**
     * Used by Waiter to return his current alternative (action to be done) or empty if there is nothing to be done.
     * @return Character representing the current alternative
     */
    public synchronized char lookAround() {
        ((Waiter)Thread.currentThread()).setWaiterState(Waiter.WaiterState.AS);
        repo.updateWaiterState(((Waiter)Thread.currentThread()).getWaiterState());
        
        while(alternative == '\0') {
            try {
                this.wait();                                                    // Waiter blocks while nothing happens
            } catch (InterruptedException ie) {}
        }
        return alternative;
    }
    /**
     *  Used by Student to inform the Waiter of his arrival and returns the Student's place of arrival.
     *  @return Element from the ArrivalOrder Enumerate
     */
    public synchronized void enter() {
        while(alternative != '\0') {
            try {
                this.wait();                                                    // Student blocks if and while Waiter is occupied (saluting other Student)
            } catch (InterruptedException ie) {}
        }
        alternative = 's';
        this.notifyAll();                                                       // Student notifies the Waiter of his arrival
    }
    /**
     *  Used by Waiter to return to the Bar and reset the alternative (action to be done).
     */
    public synchronized void returnToTheBar() { 
        alternative = '\0';
        this.notifyAll();                                                       // Waiter notifies anyone that is waiting for him
    }
    /**
     *  Used by Student to 
     */
    public synchronized void callTheWaiter() {
        
    }
    /**
     *  Used by Chef to 
     */
    public synchronized void alertTheWaiter() {
        if (((Chef)Thread.currentThread()).setChefState(Chef.ChefState.DeTP) ) {
            repo.updateChefState(((Chef)Thread.currentThread()).getChefState());
        }
        
    }
    /**
     *  Used by Student to 
     */
    public synchronized void signalTheWaiter() {
        
    }
    /**
     *  Used by Waiter to 
     */
    public synchronized void prepareTheBill() {
        if (((Waiter)Thread.currentThread()).setWaiterState(Waiter.WaiterState.PTB) ) {
            repo.updateWaiterState(((Waiter)Thread.currentThread()).getWaiterState());
        }
        
    }
    /**
     *  Used by Student to 
     */
    public synchronized void shouldHaveArrivedEarlier() {
        
    }
    /**
     *  Used by Waiter to 
     */
    public synchronized void sayGoodbye() {
        
    }
    
    /**
     *  Auxiliar Methods
     */
    
    public char getAlternative() { return alternative; }
    public void setAlternative(char alternative) { this.alternative = alternative; }
    
}