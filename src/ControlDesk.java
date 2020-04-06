/* ControlDesk.java
 *
 *  Version:
 *  		$Id$
 * 
 *  Revisions:
 * 		$Log: ControlDesk.java,v $
 * 		Revision 1.13  2003/02/02 23:26:32  ???
 * 		ControlDesk now runs its own thread and polls for free lanes to assign queue members to
 * 		
 * 		Revision 1.12  2003/02/02 20:46:13  ???
 * 		Added " 's Party" to party names.
 * 		
 * 		Revision 1.11  2003/02/02 20:43:25  ???
 * 		misc cleanup
 * 		
 * 		Revision 1.10  2003/02/02 17:49:10  ???
 * 		Fixed problem in getPartyQueue that was returning the first element as every element.
 * 		
 * 		Revision 1.9  2003/02/02 17:39:48  ???
 * 		Added accessor for lanes.
 * 		
 * 		Revision 1.8  2003/02/02 16:53:59  ???
 * 		Updated comments to match javadoc format.
 * 		
 * 		Revision 1.7  2003/02/02 16:29:52  ???
 * 		Added ControlDeskEvent and ControlDeskObserver. Updated Queue to allow access to Vector so that contents could be viewed without destroying. Implemented observer model for most of ControlDesk.
 * 		
 * 		Revision 1.6  2003/02/02 06:09:39  ???
 * 		Updated many classes to support the ControlDeskView.
 * 		
 * 		Revision 1.5  2003/01/26 23:16:10  ???
 * 		Improved thread handeling in lane/controldesk
 * 		
 * 
 */

/**
 * Class that represents control desk
 *
 */

import java.util.*;
import java.io.*;

class ControlDesk extends Thread {// Extends threads - wil provide its own thread.run method and used for multithreading

	/** The collection of Lanes */
	private HashSet lanes;

	/** The party wait queue */
	private Queue partyQueue;

	/** The number of lanes represented */
	private int numLanes;
	
	/** The collection of subscribers */
	private Vector subscribers;

    /**
     * Constructor for the ControlDesk class
     *
     * @param numlanes	the numbler of lanes to be represented
     *
     */

	public ControlDesk(int numLanes) {
		this.numLanes = numLanes;
		lanes = new HashSet(numLanes); // Set of the lanes? 
		partyQueue = new Queue(); // Queue of the people playing

		subscribers = new Vector(); // Find out

		for (int i = 0; i < numLanes; i++) {
			lanes.add(new Lane()); // Adds the lanes to the HashSet
		}
		
		this.start(); // Probably the thread.start implementation

	}
	
	/**
	 * Main loop for ControlDesk's thread
	 * 
	 */
	public void run() { // Custom run function for the main thread
		while (true) { // Infinite loop 
			
			assignLane(); // It keeps assigning lanes to parties if lanes are available 
			
			try {
				sleep(250);
			} catch (Exception e) {}
		}
	}
		

    /**
     * Retrieves a matching Bowler from the bowler database.
     *
     * @param nickName	The NickName of the Bowler
     *
     * @return a Bowler object.
     *
     */

	private Bowler registerPatron(String nickName) {
		Bowler patron = null;

		try {
			// only one patron / nick.... no dupes, no checks

			patron = BowlerFile.getBowlerInfo(nickName); // Function from the BowerFile class to find the Bowler

		} catch (FileNotFoundException e) {
			System.err.println("Error..." + e);
		} catch (IOException e) {
			System.err.println("Error..." + e);
		}

		return patron;
		// Returns the Bowler object if details are found ? 
		// Else returns a nulll object 
	}

    /**
     * Iterate through the available lanes and assign the paties in the wait queue if lanes are available.
     *
     */

	public void assignLane() {
		Iterator it = lanes.iterator(); // Iterator that goes through the lanes HashSet in any random order 

		while (it.hasNext() && partyQueue.hasMoreElements()) {
			Lane curLane = (Lane) it.next();

			if (curLane.isPartyAssigned() == false) {
				System.out.println("ok... assigning this party");
				curLane.assignParty(((Party) partyQueue.next())); // Function from the Lane class to assign Party
			}
		}
		publish(new ControlDeskEvent(getPartyQueue())); // Send the ControlDeskEvent to all the subscribers
	}

    /**
     */

	public void viewScores(Lane ln) {
		// TODO: attach a LaneScoreView object to that lane
	}

    /**
     * Creates a party from a Vector of nickNAmes and adds them to the wait queue.
     *
     * @param partyNicks	A Vector of NickNames
     *
     */

	public void addPartyQueue(Vector partyNicks) {
		Vector partyBowlers = new Vector();
		// Iterate through the list of the party nicknames and then create a new Bowler from the nickName 
		// Then add them to a partyBowlers list
		for (int i = 0; i < partyNicks.size(); i++) {
			Bowler newBowler = registerPatron(((String) partyNicks.get(i))); // Calls the register patron function that was declared above
			partyBowlers.add(newBowler); // newBowler should not be null... Hopefully, I guess, the function will add new if doesn't exist.
		}
		Party newParty = new Party(partyBowlers); // Some Party object is created with the required details
		partyQueue.add(newParty); // Adds the party to the current party queue
		publish(new ControlDeskEvent(getPartyQueue())); // Sends the ControlDeskEvent to all the subscribers
 	}

    /**
     * Returns a Vector of party names to be displayed in the GUI representation of the wait queue.
	 *
     * @return a Vecotr of Strings
     *
     */

	public Vector getPartyQueue() {
		Vector displayPartyQueue = new Vector(); // New vector to be returned
		// Loops through the partyQueue (Queue) by converting it to a Vector 
		for ( int i=0; i < ( (Vector)partyQueue.asVector()).size(); i++ ) {
			String nextParty =
				((Bowler) ((Vector) ((Party) partyQueue.asVector().get( i ) ).getMembers())  // List of all the members of the group
					.get(0))  // Gets the first bowler
					.getNickName() + "'s Party"; // So the string <FirstBowlerNickname>'s Party
			displayPartyQueue.addElement(nextParty); // Adds that to the displayPartyQueue
		}
		// Returns a list of strings as created above
		return displayPartyQueue;
	}

    /**
     * Accessor for the number of lanes represented by the ControlDesk
     * 
     * @return an int containing the number of lanes represented
     *
     */

	// Okay to have this because numLanes is a private variable
	public int getNumLanes() {
		return numLanes;
	}

    /**
     * Allows objects to subscribe as observers
     * 
     * @param adding	the ControlDeskObserver that will be subscribed
     *
     */

	//  Subscription? Observers?
	public void subscribe(ControlDeskObserver adding) {
		subscribers.add(adding); // Pushes it into the subscribers list
	}

    /**
     * Broadcast an event to subscribing objects.
     * 
     * @param event	the ControlDeskEvent to broadcast
     *
     */

	public void publish(ControlDeskEvent event) {
		Iterator eventIterator = subscribers.iterator(); // Iterator of the subscribers vector
		while (eventIterator.hasNext()) {
			(
				(ControlDeskObserver) eventIterator
					.next())
					.receiveControlDeskEvent( // Find out? // Sends the ControlDesKEvent to the receiver at the other end
				event);
		}
	}

    /**
     * Accessor method for lanes
     * 
     * @return a HashSet of Lanes
     *
     */

	// Don't know if the lanes object is private or public?
	public HashSet getLanes() {
		return lanes;
	}
}
