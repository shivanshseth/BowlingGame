
/* $Id$
 *
 * Revisions:
 *   $Log: Lane.java,v $
 *   Revision 1.52  2003/02/20 20:27:45  ???
 *   Fouls disables.
 *
 *   Revision 1.51  2003/02/20 20:01:32  ???
 *   Added things.
 *
 *   Revision 1.50  2003/02/20 19:53:52  ???
 *   Added foul support.  Still need to update laneview and test this.
 *
 *   Revision 1.49  2003/02/20 11:18:22  ???
 *   Works beautifully.
 *
 *   Revision 1.48  2003/02/20 04:10:58  ???
 *   Score reporting code should be good.
 *
 *   Revision 1.47  2003/02/17 00:25:28  ???
 *   Added disbale controls for View objects.
 *
 *   Revision 1.46  2003/02/17 00:20:47  ???
 *   fix for event when game ends
 *
 *   Revision 1.43  2003/02/17 00:09:42  ???
 *   fix for event when game ends
 *
 *   Revision 1.42  2003/02/17 00:03:34  ???
 *   Bug fixed
 *
 *   Revision 1.41  2003/02/16 23:59:49  ???
 *   Reporting of sorts.
 *
 *   Revision 1.40  2003/02/16 23:44:33  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.39  2003/02/16 23:43:08  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.38  2003/02/16 23:41:05  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.37  2003/02/16 23:00:26  ???
 *   added mechnanical problem flag
 *
 *   Revision 1.36  2003/02/16 21:31:04  ???
 *   Score logging.
 *
 *   Revision 1.35  2003/02/09 21:38:00  ???
 *   Added lots of comments
 *
 *   Revision 1.34  2003/02/06 00:27:46  ???
 *   Fixed a race condition
 *
 *   Revision 1.33  2003/02/05 11:16:34  ???
 *   Boom-Shacka-Lacka!!!
 *
 *   Revision 1.32  2003/02/05 01:15:19  ???
 *   Real close now.  Honest.
 *
 *   Revision 1.31  2003/02/04 22:02:04  ???
 *   Still not quite working...
 *
 *   Revision 1.30  2003/02/04 13:33:04  ???
 *   Lane may very well work now.
 *
 *   Revision 1.29  2003/02/02 23:57:27  ???
 *   fix on pinsetter hack
 *
 *   Revision 1.28  2003/02/02 23:49:48  ???
 *   Pinsetter generates an event when all pins are reset
 *
 *   Revision 1.27  2003/02/02 23:26:32  ???
 *   ControlDesk now runs its own thread and polls for free lanes to assign queue members to
 *
 *   Revision 1.26  2003/02/02 23:11:42  ???
 *   parties can now play more than 1 game on a lane, and lanes are properly released after games
 *
 *   Revision 1.25  2003/02/02 22:52:19  ???
 *   Lane compiles
 *
 *   Revision 1.24  2003/02/02 22:50:10  ???
 *   Lane compiles
 *
 *   Revision 1.23  2003/02/02 22:47:34  ???
 *   More observering.
 *
 *   Revision 1.22  2003/02/02 22:15:40  ???
 *   Add accessor for pinsetter.
 *
 *   Revision 1.21  2003/02/02 21:59:20  ???
 *   added conditions for the party choosing to play another game
 *
 *   Revision 1.20  2003/02/02 21:51:54  ???
 *   LaneEvent may very well be observer method.
 *
 *   Revision 1.19  2003/02/02 20:28:59  ???
 *   fixed sleep thread bug in lane
 *
 *   Revision 1.18  2003/02/02 18:18:51  ???
 *   more changes. just need to fix scoring.
 *
 *   Revision 1.17  2003/02/02 17:47:02  ???
 *   Things are pretty close to working now...
 *
 *   Revision 1.16  2003/01/30 22:09:32  ???
 *   Worked on scoring.
 *
 *   Revision 1.15  2003/01/30 21:45:08  ???
 *   Fixed speling of received in Lane.
 *
 *   Revision 1.14  2003/01/30 21:29:30  ???
 *   Fixed some MVC stuff
 *
 *   Revision 1.13  2003/01/30 03:45:26  ???
 *   *** empty log message ***
 *
 *   Revision 1.12  2003/01/26 23:16:10  ???
 *   Improved thread handeling in lane/controldesk
 *
 *   Revision 1.11  2003/01/26 22:34:44  ???
 *   Total rewrite of lane and pinsetter for R2's observer model
 *   Added Lane/Pinsetter Observer
 *   Rewrite of scoring algorythm in lane
 *
 *   Revision 1.10  2003/01/26 20:44:05  ???
 *   small changes
 *
 * 
 */

import java.util.Vector;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Date;

public class Lane extends Thread implements PinsetterObserver {	// Implements PinsetterObserver and extends Thread
	private Party party;
	private Pinsetter setter;
	private HashMap scores;
	private Vector subscribers;

	private boolean gameIsHalted;

	private boolean partyAssigned;
	private boolean gameFinished;
	private Iterator bowlerIterator;
	private int ball;
	private int bowlIndex;
	private int frameNumber;
	private boolean tenthFrameStrike;

	private int[] curScores;
	private int[][] cumulScores;
	private boolean canThrowAgain;
	
	private int[][] finalScores;
	private int gameNumber;
	
	private Bowler currentThrower;			// = the thrower who just took a throw

	/** Lane()
	 * 
	 * Constructs a new lane and starts its thread
	 * 
	 * @pre none
	 * @post a new lane has been created and its thered is executing
	 */
	// Constructor for the Lane Object
	public Lane() {
		setter = new Pinsetter(); // Assign a new PinSetter Object
		scores = new HashMap(); // Creates a HashMap of the scores
		subscribers = new Vector();

		gameIsHalted = false;
		partyAssigned = false;

		gameNumber = 0;

		setter.subscribe( this ); // Subscribes the Pinsetter object to this ?
		
		this.start(); // Start from the Threads to run for the Lane
	}


	private void generateFinalScores() {
		finalScores[bowlIndex][gameNumber] = cumulScores[bowlIndex][9]; //final score of the bowler/gameNumber is given by cumulscores of bowler -> 9 (10th frame I presume)
		try{
			Date date = new Date();
			String dateString = "" + date.getHours() + ":" + date.getMinutes() + " " + date.getMonth() + "/" + date.getDay() + "/" + (date.getYear() + 1900); // Extract this expression?
			ScoreHistoryFile.addScore(currentThrower.getNick(), dateString, Integer.toString(cumulScores[bowlIndex][9]));  // Writes to file for the bowler
		} catch (Exception e) {System.err.println("Exception in addScore. "+ e );}
	}

	private void oneThrow() {
		currentThrower = (Bowler) bowlerIterator.next();

		canThrowAgain = true;
		tenthFrameStrike = false;
		ball = 0;

		while (canThrowAgain) {
			setter.ballThrown();
			ball++;
		}
	}

	private void updateFrame() {
		frameNumber++; // Else it goes to the next frame
		resetBowlerIterator(); // Resets the iterator
		bowlIndex = 0; // Resets the bowler playing at the moment
		if (frameNumber > 9) {
			gameFinished = true;
			gameNumber++; // Check where this is referenced further
		}
	}

	private void haltedCycle() {
		while (gameIsHalted) { // While the game is halted stop the run and run empty loop
			try {
				sleep(10);
			} catch (Exception e) {}
		}
	}

	private Vector endGameReports () {
		Vector printVector;
		EndGameReport egr = new EndGameReport( ((Bowler)party.getMembers().get(0)).getNickName() + "'s Party", party); // Report Page
		printVector = egr.getResult(); // Gets the results as a Vector
		partyAssigned = false; // Resets the party assigned to the lane to false --> Fixed number of lanes pushed into the HashSet initally
//		Iterator scoreIt = party.getMembers().iterator();
//		partyAssigned = false;
//		party = null;
		return printVector;
	}


	private boolean isPartyOnLane() {
		return partyAssigned && !gameFinished;
	}

	private boolean isGameEnded() {
		return partyAssigned && gameFinished;
	}

	private void whilePartyOnLane() {
		if(isPartyOnLane()) {
			haltedCycle();

			if(bowlerIterator.hasNext()) {
				oneThrow();

				if(frameNumber == 9) {
					generateFinalScores();
				}

				setter.reset();
				bowlIndex++;
			}
			else {
				updateFrame();
			}
		}
	}

	private void printBowlerScore(Bowler thisBowler, Iterator printIt, ScoreReport sr) {
		while (printIt.hasNext()){
			if (thisBowler.getNick() == (String)printIt.next()){
				System.out.println("Printing " + thisBowler.getNick());
				sr.sendPrintout(); // Printing the results
			}
		}
	}


	private void printPartyBowlerScore(Vector printVector, Iterator scoreIt) {
		int myIndex = 0;
		while (scoreIt.hasNext()){ // While the next exists
			Bowler thisBowler = (Bowler)scoreIt.next(); // Get the bowler
			ScoreReport sr = new ScoreReport( thisBowler, finalScores[myIndex++], gameNumber ); // New ScoreReport for the bowler with the parameters : Bowler, score, and gameNumber
			sr.sendEmail(thisBowler.getEmail()); // Some send email function
			Iterator printIt = printVector.iterator();

			printBowlerScore(thisBowler, printIt, sr);
		}
	}

	private void performPostPromptOutput(int result) {
		if (result == 1) {
			resetBowlerIterator();
			resetScores();
		}
		else if(result == 2) { // Can I remove this if and make it just else? Will remove 2 more from the complexity
			Vector printVector = endGameReports();
			Iterator scoreIt = party.getMembers().iterator();
			partyAssigned = false;
			party = null;
			publish(lanePublish()); // publish ?
			printPartyBowlerScore(printVector, scoreIt);
		}
	}

	private void whenGameEnded() {
		if(isGameEnded()) {
			EndGamePrompt egp = new EndGamePrompt( ((Bowler) party.getMembers().get(0)).getNickName() + "'s Party" ); // Creates the prompt to start another game
													// Chaining happening here
			int result = egp.getResult(); // Yes or no... I guess
			egp.distroy();  // Destroys the egp
			egp = null; // Sets it to null


			System.out.println("result was: " + result);

			performPostPromptOutput(result);

		}
	}


	/** run()
	 *
	 * entry point for execution of this lane
	 */
	public void run() {

		while (true) {
			whilePartyOnLane();
			whenGameEnded();
//			if (isPartyOnLane()) {	// we have a party on this lane,
//								// so next bower can take a throw
//
////				while (gameIsHalted) { // While the game is halted stop the run and run empty loop
////					try {
////						sleep(10);
////					} catch (Exception e) {}
////				}
//
//				haltedCycle();
//
//				// It goes through all the bowler for a particular frame
//				if (bowlerIterator.hasNext()) { // If the bowlerIterator has a next element
////					currentThrower = (Bowler)bowlerIterator.next(); // Assign the thrower to the next one in the list
////
////					canThrowAgain = true;
////					tenthFrameStrike = false;
////					ball = 0;
////					while (canThrowAgain) {
////						setter.ballThrown();		// simulate the thrower's ball hiting
////						ball++;
////						// The only way this seems to stop would be when there would be some kind of break statement or the setter.ballThrown updates the while command.
////					}
//					oneThrow();
//
//					if (frameNumber == 9){
//						// This seems to assign the total score of the bowler
////						finalScores[bowlIndex][gameNumber] = cumulScores[bowlIndex][9]; //final score of the bowler/gameNumber is given by cumulscores of bowler -> 9 (10th frame I presume)
////						try{
////							Date date = new Date();
////							String dateString = "" + date.getHours() + ":" + date.getMinutes() + " " + date.getMonth() + "/" + date.getDay() + "/" + (date.getYear() + 1900);
////							ScoreHistoryFile.addScore(currentThrower.getNick(), dateString, new Integer(cumulScores[bowlIndex][9]).toString());  // Writes to file for the bowler
////						} catch (Exception e) {System.err.println("Exception in addScore. "+ e );}
//
//						generateFinalScores();
//					}
//
//
//					setter.reset(); // Setter.reset() ?
//					bowlIndex++; // BowlIndex++ -> Changes the bowler currently running the thing
//
//				} else {
////					frameNumber++; // Else it goes to the next frame
////					resetBowlerIterator(); // Resets the iterator
////					bowlIndex = 0; // Resets the bowler playing at the moment
////					if (frameNumber > 9) {
////						gameFinished = true;
////						gameNumber++; // Check where this is referenced further
////					}
//
//					updateFrame();
//				}
//			} else if (isGameEnded()) {
//				EndGamePrompt egp = new EndGamePrompt( ((Bowler) party.getMembers().get(0)).getNickName() + "'s Party" ); // Creates the prompt to start another game
//				int result = egp.getResult(); // Yes or no... I guess
//				egp.distroy();  // Destroys the egp
//				egp = null; // Sets it to null
//
//
//				System.out.println("result was: " + result);
//
//				// TODO: send record of scores to control desk
//				if (result == 1) {					// yes, want to play again
//					resetScores(); // Just restarts the entire process by resetting the scores
//					resetBowlerIterator(); // Then resets the bowler iterator
//
//				} else if (result == 2) {// no, dont want to play another game
////					Vector printVector;
////					party = null;
////					EndGameReport egr = new EndGameReport( ((Bowler)party.getMembers().get(0)).getNickName() + "'s Party", party); // Report Page
////					printVector = egr.getResult(); // Gets the results as a Vector
////					partyAssigned = false; // Resets the party assigned to the lane to false --> Fixed number of lanes pushed into the HashSet initally
////					Iterator scoreIt = party.getMembers().iterator();
////					partyAssigned = false;
//
//					Vector printVector = endGameReports();
//
//					publish(lanePublish()); // publish ?
//
//					printPartyBowlerScore();
////					while (scoreIt.hasNext()){ // While the next exists
////						Bowler thisBowler = (Bowler)scoreIt.next(); // Get the bowler
////						ScoreReport sr = new ScoreReport( thisBowler, finalScores[myIndex++], gameNumber ); // New ScoreReport for the bowler with the parameters : Bowler, score, and gameNumber
////						sr.sendEmail(thisBowler.getEmail()); // Some send email function
////						Iterator printIt = printVector.iterator();
////
////						printBowlerScore(thisBowler, printIt);
////
////					}
////				}
//			}


			try {
				sleep(10);
			} catch (Exception e) {}
		}
	}

	private boolean isAllPinsDown(int first, int second) {
		// Second may be 0 for checking Strike
		// Else second will be > 0
		return first + second == 10;
	}

	private boolean isSecondThrow(PinsetterEvent pe) {
		return pe.getThrowNumber() == 2;
	}

	private void updateTenthFrameThirdThrowPossibility(PinsetterEvent pe) {
		if (!isAllPinsDown(pe.totalPinsDown(), 0) && (isSecondThrow(pe) && !tenthFrameStrike) ) {
			canThrowAgain = false;
		}
	}


	// Do I really need to use this function?
//	public boolean isLastFrame() {
//		return frameNumber == 9;
//	}

	private void updateNormalFrame(PinsetterEvent pe) {
		if (isAllPinsDown(pe.pinsDownOnThisThrow(), 0)) {		// threw a strike
			canThrowAgain = false;
			//publish( lanePublish() );
		} else if (isSecondThrow(pe)) { // If it is the third
			canThrowAgain = false;
		}
		//publish( lanePublish() );
		assert pe.getThrowNumber() != 3 : "I'm here...";
	}

	private void handleTenthFrameStrike(PinsetterEvent pe) {
		setter.resetPins(); // Resets the pins
		if(pe.getThrowNumber() == 1) { // If it is the second throw (or first throw --> check indexing)?
			tenthFrameStrike = true; // Assigns strike to the tenth frame
		}
	}

	private void updateTenthFrame(PinsetterEvent pe) {
		if (isAllPinsDown(pe.totalPinsDown(), 0)) { // If it is a strike
			handleTenthFrameStrike(pe);
		}

		updateTenthFrameThirdThrowPossibility(pe);

		if (pe.getThrowNumber() == 3) { // If it the 4th chance then we have to not allow the person to throw irrespective of the strike or not
			canThrowAgain = false; // Makes the possibility to throw again to false
			//publish( lanePublish() );
		}

	}


	/** recievePinsetterEvent()
	 *
	 * recieves the thrown event from the pinsetter
	 *
	 * @pre none
	 * @post the event has been acted upon if desiered
	 *
	 * @param pe 		The pinsetter event that has been received.
	 */
	public void receivePinsetterEvent(PinsetterEvent pe) {

			if (pe.pinsDownOnThisThrow() >=  0) {			// this is a real throw
				markScore(currentThrower, frameNumber + 1, pe.getThrowNumber(), pe.pinsDownOnThisThrow()); // marks the score of the bowler on the frame

				// next logic handles the ?: what conditions dont allow them another throw?
				// handle the case of 10th frame first
				if (frameNumber == 9) { // Checks if it is the last frame --> Zero based indexing
//					if (isAllPinsDown(pe.totalPinsDown(), 0)) { // If it is a strike
//						setter.resetPins(); // Resets the pins
//						if(pe.getThrowNumber() == 1) { // If it is the second throw (or first throw --> check indexing)?
//							tenthFrameStrike = true; // Assigns strike to the tenth frame
//						}
//					}
//
////				if (isThirdThrowNotPossible(pe)) { // Checks if it is the third chance for the 10th frame and if it is not a strike
////					canThrowAgain = false; // Makes the possibility to throw again to false
////					//publish( lanePublish() );
////				}
//
//					updateTenthFrameThirdThrowPossibility(pe);
//
//					if (pe.getThrowNumber() == 3) { // If it the 4th chance then we have to not allow the person to throw irrespective of the strike or not
//						canThrowAgain = false; // Makes the possibility to throw again to false
//						//publish( lanePublish() );
//					}
					updateTenthFrame(pe);

				} else { // its not the 10th frame
					// Maybe we can make this a switch statement
//					if (isAllPinsDown(pe.pinsDownOnThisThrow(), 0)) {		// threw a strike
//						canThrowAgain = false;
//						//publish( lanePublish() );
//					} else if (isSecondThrow()Throw(pe)) { // If it is the third
//						canThrowAgain = false;
//					}
//					//publish( lanePublish() );
////					else if (pe.getThrowNumber() == 3)   // Shouldn't reach here.
////						System.out.println("I'm here..."); // Error recognizing statement
//					assert pe.getThrowNumber() != 3 : "I'm here...";
					updateNormalFrame(pe);
				}
			} else {								//  this is not a real throw, probably a reset
			}
	}

	/** resetBowlerIterator()
	 *
	 * sets the current bower iterator back to the first bowler
	 *
	 * @pre the party as been assigned
	 * @post the iterator points to the first bowler in the party
	 */
	private void resetBowlerIterator() {
		bowlerIterator = (party.getMembers()).iterator(); // Gets the inital iterator of the bowler Iterator and deals with a global variable bowlerItertor
	}

	/** resetScores()
	 *
	 * resets the scoring mechanism, must be called before scoring starts
	 *
	 * @pre the party has been assigned
	 * @post scoring system is initialized
	 */
	private void resetScores() {
		Iterator bowlIt = (party.getMembers()).iterator(); // Gets the iterator for the party -> all the bowlers

		while ( bowlIt.hasNext() ) { // If there is a next player in the party
			int[] toPut = new int[25]; // I don't know why that is 25 --> 22 rounds in total
			for ( int i = 0; i != 25; i++){
				toPut[i] = -1;
			}
			scores.put( bowlIt.next(), toPut ); // Puts -1 to all the frames for the bowler
		}



		gameFinished = false; // Restarts the game
		frameNumber = 0; // Frame 0
	}

	/** assignParty()
	 *
	 * assigns a party to this lane
	 *
	 * @pre none
	 * @post the party has been assigned to the lane
	 *
	 * @param theParty		Party to be assigned
	 */
	public void assignParty( Party theParty ) {
		party = theParty;
		resetBowlerIterator(); // Gets the iterator of the the party
		partyAssigned = true;

		curScores = new int[party.getMembers().size()]; // Score of the current frame ?
		cumulScores = new int[party.getMembers().size()][10]; // Makes the cumulScore of the the 10 frames
		finalScores = new int[party.getMembers().size()][128]; //Hardcoding a max of 128 games, bite me. --> Can we change that by doubling the values everytime it causes a problem
		gameNumber = 0; // Game number --> Useful for the finalScores

		resetScores(); // Get the score of (unplayed... I presume --> -1)
	}

	public int getIndex(int frame, int ball){
		return 2 * (frame - 1) + ball;
	}

	/** markScore()
	 *
	 * Method that marks a bowlers score on the board.
	 *
	 * @param Cur		The current bowler
	 * @param frame	The frame that bowler is on
	 * @param ball		The ball the bowler is on
	 * @param score	The bowler's score
	 */
	private void markScore( Bowler Cur, int frame, int ball, int score ){
		int[] curScore;
		int index =  getIndex(frame, ball); // Finds the index of the throw -> Row wise ordering type

		curScore = (int[]) scores.get(Cur); // Gets the score of the player


		curScore[ index - 1] = score; // Assigns the score of the throw to the curScore array
		scores.put(Cur, curScore); // Puts the score back to the scores thing
		// Is getting it, then modifying better than modifying inplace? --> Can that even be done
		getScore( Cur, frame ); // gets the score of the Bowler for the frame
		publish( lanePublish() ); // Publish?
	}

	/** lanePublish()
	 *
	 * Method that creates and returns a newly created laneEvent
	 *
	 * @return		The new lane event
	 */
	private LaneEvent lanePublish(  ) {
		return new LaneEvent(party, bowlIndex, currentThrower, cumulScores, scores, frameNumber+1, curScores, ball, gameIsHalted);
		// Hypothesis -> All event things are used as state management that a newer event assigns a newer state
		// Publish function I presume puts out the event to the all the required functions to use
	}

	private boolean isSecond(int i) {
		return i % 2 == 1;
	}

	private boolean iLessThanCurrent(int i, int current, int c) {
		return  i < (current - c);
	}

	private boolean iLessThan(int i, int c){
		return i < c;
	}

	private boolean isSpare(int i, int first, int second, int current) {
		return  isSecond(i) && isAllPinsDown(first, second) && iLessThanCurrent(i, current, 1) && iLessThan(i, 19);
	}

	private boolean isStrike(int i, int first, int current) {
		return  !isSecond(i) && isAllPinsDown(first, 0) && iLessThanCurrent(i, current, 0) && iLessThan(i, 18);
	}


	private void doSpareCycle(int i, int prev, int curr, int next, int current) {
		if(isSpare(i, prev, curr, current)) {
			//This ball was a the second of a spare.
			//Also, we're not on the current ball.
			//Add the next ball to the ith one in cumul.
			cumulScores[bowlIndex][(i/2)] += curr + next;
		}
	}

	private boolean isnotEqualtoC(int a, int c) {
		return a != c;
	}

	private void addNext2(int next2, int halfI) {
		if (isnotEqualtoC(next2, -1) && isnotEqualtoC(next2, -2)) {
			cumulScores[bowlIndex][(halfI)] += next2;
		}
	}

	private void addNext3(int next2,int next3, int halfI) {
		if(isnotEqualtoC(next2, -1) && isnotEqualtoC(next3, -2)) {
			cumulScores[bowlIndex][(halfI)] += next3;
		}
	}

	private void performAddingNextTwo(int next1, int next2, int next3, int halfI) {
		if(isnotEqualtoC(next1, -1)) {
			cumulScores[bowlIndex][halfI] += next1 + cumulScores[bowlIndex][halfI - 1];
			addNext2(next2, halfI);
			addNext3(next2, next3, halfI);
		}
	}

	private void resetCumulScores(){
		for (int i = 0; i != 10; i++){
			cumulScores[bowlIndex][i] = 0;
		}
	}

	private void performAddingNextTwoPart2(int next1, int next2, int next3, int next4, int halfI) {
		if ( halfI > 0 ){
			cumulScores[bowlIndex][halfI] += next2 + cumulScores[bowlIndex][halfI-1];
		} else {
			cumulScores[bowlIndex][halfI] += next2;
		}
		if (isnotEqualtoC(next3, -1)){
			if( isnotEqualtoC(next3, -2)){
				cumulScores[bowlIndex][halfI] += next3;
			}
		} else {
			cumulScores[bowlIndex][halfI] += next4;
		}
	}

	/** getScore()
	 *
	 * Method that calculates a bowlers score
	 * 
	 * @param Cur		The bowler that is currently up
	 * @param frame	The frame the current bowler is on
	 * 
	 * @return			The bowlers total score
	 */
	private int getScore( Bowler Cur, int frame) {
		int[] curScore;
		int strikeballs = 0;
		int totalScore = 0;
		curScore = (int[]) scores.get(Cur); // Gets the current score of the plater
//		for (int i = 0; i != 10; i++){
//			cumulScores[bowlIndex][i] = 0; // Uses the global variable bowlIndex --> zeroes out the cumulScore thing
//		}
		resetCumulScores();
		int current = getIndex(frame, ball) - 1;
		//Iterate through each ball until the current one.
		for (int i = 0; i != current+2; i++){
			if (test1(curScore, current, i)) break;
		}
		return totalScore;
	}

	private boolean test1(int[] curScore, int current, int i) {
		int strikeballs;// Utils
		int halfI = i/2;
		//Spare:
		if(i % 2 == 1)
			doSpareCycle(i, curScore[i-1], curScore[i], curScore[i + 1], current);
//			if(isSpare(i, curScore[i - 1], curScore[i], current)){
		//This ball was a the second of a spare.
		//Also, we're not on the current ball.
		//Add the next ball to the ith one in cumul.
//				cumulScores[bowlIndex][(i/2)] += curScore[i+1] + curScore[i];
//				if (i > 1) { 																// Why is this here?
//					//cumulScores[bowlIndex][i/2] += cumulScores[bowlIndex][i/2 -1];
//				}
//			}
		if(isStrike(i, curScore[i], current)){
			strikeballs = 0;
			//This ball is the first ball, and was a strike.
			//If we can get 2 balls after it, good add them to cumul.
			int next1 = curScore[i + 1];
			int next2 = curScore[i + 2];
			int next3 = curScore[i + 3];
			int next4 = curScore[i + 4];

			if (isnotEqualtoC(next2, -1)) {
				strikeballs = 1;
				if(isnotEqualtoC(next3, -1)) {
					//Still got em.
					strikeballs = 2;
				} else if(isnotEqualtoC(next4, -1)) {
					//Ok, got it.
					strikeballs = 2;
				}
			}

			if (strikeballs == 2){
				//Add up the strike.
				//Add the next two balls to the current cumulscore.
				cumulScores[bowlIndex][halfI] += 10;
				performAddingNextTwo(next1, next2, next3, next4);
				performAddingNextTwoPart2(next1, next2, next3, next4, halfI);
//						if ( halfI > 0 ){
//							cumulScores[bowlIndex][halfI] += next2 + cumulScores[bowlIndex][halfI-1];
//						} else {
//							cumulScores[bowlIndex][halfI] += next2;
//						}
//						if (isnotEqualtoC(next3, -1)){
//							if( isnotEqualtoC(next3, -2)){
//								cumulScores[bowlIndex][halfI] += next3;
//							}
//						} else {
//							cumulScores[bowlIndex][halfI] += next4;
//						}
			} else {
				return true;
			}
		}else {
			//We're dealing with a normal throw, add it and be on our way.
			test2(curScore, i, halfI);
		}
		return false;
	}

	private void test2(int[] curScore, int i, int halfI) {
		test3(curScore, i, halfI);
		if (halfI == 9){
			if (i == 18){
				cumulScores[bowlIndex][9] += cumulScores[bowlIndex][8];
			}
			if(isnotEqualtoC(curScore[i], -2)){
				cumulScores[bowlIndex][9] += curScore[i];
			}
		} else if (halfI == 10) {
			if(isnotEqualtoC(curScore[i], -2)){
				cumulScores[bowlIndex][9] += curScore[i];
			}
		}
	}

	private void test3(int[] curScore, int i, int halfI) {
		if( !isSecond(i) && iLessThan(i, 18)){
			test4(curScore, i, halfI);
		} else if (iLessThan(i, 18)){
			if(curScore[i] != -1 && i > 2){
				if(isnotEqualtoC(curScore[i], -2)){
					cumulScores[bowlIndex][halfI] += curScore[i];
				}
			}
		}
	}

	private void test4(int[] curScore, int i, int halfI) {
		if ( halfI == 0 ) {
			//First frame, first ball.  Set his cumul score to the first ball
			if(isnotEqualtoC(curScore[i], -2)){
				cumulScores[bowlIndex][halfI] += curScore[i];
			}
		} else if (halfI != 9){
			//add his last frame's cumul to this ball, make it this frame's cumul.
			if(isnotEqualtoC(curScore[i], -2)){
				cumulScores[bowlIndex][halfI] += cumulScores[bowlIndex][halfI - 1] + curScore[i];
			} else {
				cumulScores[bowlIndex][halfI] += cumulScores[bowlIndex][halfI - 1];
			}
		}
	}

	/** isPartyAssigned()
	 * 
	 * checks if a party is assigned to this lane
	 * 
	 * @return true if party assigned, false otherwise
	 */
	public boolean isPartyAssigned() {
		return partyAssigned;
	}
	
	/** isGameFinished
	 * 
	 * @return true if the game is done, false otherwise
	 */
	public boolean isGameFinished() {
		return gameFinished;
	}

	/** subscribe
	 * 
	 * Method that will add a subscriber
	 * 
//	 * @param subscribe	Observer that is to be added
	 */

	public void subscribe( LaneObserver adding ) {
		subscribers.add( adding );
	}

	/** unsubscribe
	 * 
	 * Method that unsubscribes an observer from this object
	 * 
	 * @param removing	The observer to be removed
	 */
	
	public void unsubscribe( LaneObserver removing ) {
		subscribers.remove( removing );
	}

	/** publish
	 *
	 * Method that publishes an event to subscribers
	 * 
	 * @param event	Event that is to be published
	 */

	public void publish( LaneEvent event ) {
		if( subscribers.size() > 0 ) {
			Iterator eventIterator = subscribers.iterator(); // Create an iterator for the subscribers
			
			while ( eventIterator.hasNext() ) {
				( (LaneObserver) eventIterator.next()).receiveLaneEvent( event ); // for the subscribers send the Lane Event
			}
		}
	}

	/**
	 * Accessor to get this Lane's pinsetter
	 * 
	 * @return		A reference to this lane's pinsetter
	 */

	public Pinsetter getPinsetter() {
		return setter;	
	}

	/**
	 * Pause the execution of this game
	 */
	public void pauseGame() {
		gameIsHalted = true;
		publish(lanePublish()); // Send the update to all the subscribers
	}
	
	/**
	 * Resume the execution of this game
	 */
	public void unPauseGame() {
		gameIsHalted = false;
		publish(lanePublish()); // Send the update to all the subscribers
	}

}
