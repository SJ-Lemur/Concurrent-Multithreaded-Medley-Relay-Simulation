
//Class to represent a swimmer swimming a race
//Swimmers have one of four possible swim strokes: backstroke, breaststroke, butterfly and freestyle
package medleySimulation;

import java.awt.Color;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;

public class Swimmer extends Thread {
	
	public static StadiumGrid stadium; //shared 
	private FinishCounter finish; //shared
	
		
	GridBlock currentBlock;
	private Random rand;
	private int movingSpeed;
	
	private PeopleLocation myLocation;
	private int ID; //thread ID 
	private int team; // team ID
	private GridBlock start;

	public enum SwimStroke { 
		Backstroke(1,2.5,Color.black),
		Breaststroke(2,2.1,new Color(255,102,0)),
		Butterfly(3,2.55,Color.magenta),
		Freestyle(4,2.8,Color.red);
	    	
	     private final double strokeTime;
	     private final int order; // in minutes
	     private final Color colour;   

	     SwimStroke( int order, double sT, Color c) {
	            this.strokeTime = sT;
	            this.order = order;
	            this.colour = c;
	        }
	  
	        public int getOrder() {return order;}

	        public  Color getColour() { return colour; }
	    }  
	    private final SwimStroke swimStroke;
	
	//Constructor
	Swimmer( int ID, int t, PeopleLocation loc, FinishCounter f, int speed, SwimStroke s) {
		this.swimStroke = s;
		this.ID=ID;
		movingSpeed=speed; //range of speeds for swimmers
		this.myLocation = loc;
		this.team=t;
		start = stadium.returnStartingBlock(team);
		finish=f;
		rand=new Random();
	}
	
	//getter
	public   int getX() { return currentBlock.getX();}	
	
	//getter
	public   int getY() {	return currentBlock.getY();	}
	
	//getter
	public   int getSpeed() { return movingSpeed; }

	
	public SwimStroke getSwimStroke() {
		return swimStroke;
	}

	//!!!You do not need to change the method below!!!
	//swimmer enters stadium area
	public void enterStadium() throws InterruptedException {
		currentBlock = stadium.enterStadium(myLocation);  //
		sleep(200);  //wait a bit at door, look around
	}
	
	//!!!You do not need to change the method below!!!
	//go to the starting blocks
	//printlns are left here for help in debugging
	public void goToStartingBlocks() throws InterruptedException {		
		int x_st= start.getX();
		int y_st= start.getY();
	//System.out.println("Thread "+this.ID + " has start position: " + x_st  + " " +y_st );
	// System.out.println("Thread "+this.ID + " at " + currentBlock.getX()  + " " +currentBlock.getY() );
	 while (currentBlock!=start) {
		//	System.out.println("Thread "+this.ID + " has starting position: " + x_st  + " " +y_st );
		//	System.out.println("Thread "+this.ID + " at position: " + currentBlock.getX()  + " " +currentBlock.getY() );
			sleep(movingSpeed*3);  //not rushing 
			currentBlock=stadium.moveTowards(currentBlock,x_st,y_st,myLocation); //head toward starting block
		//	System.out.println("Thread "+this.ID + " moved toward start to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
		}
	//System.out.println("-----------Thread "+this.ID + " at start " + currentBlock.getX()  + " " +currentBlock.getY() );
	}
	
	//!!!You do not need to change the method below!!!
	//dive in to the pool
	private void dive() throws InterruptedException {
		int x= currentBlock.getX();
		int y= currentBlock.getY();
		currentBlock=stadium.jumpTo(currentBlock,x,y-2,myLocation);
	}
	
	//!!!You do not need to change the method below!!!
	//swim there and back
	private void swimRace() throws InterruptedException {
		int x= currentBlock.getX();
		while((boolean) ((currentBlock.getY())!=0)) {
			currentBlock=stadium.moveTowards(currentBlock,x,0,myLocation);
			//System.out.println("Thread "+this.ID + " swimming " + currentBlock.getX()  + " " +currentBlock.getY() );
			sleep((int) (movingSpeed*swimStroke.strokeTime)); //swim
			System.out.println("Thread "+this.ID + " swimming  at speed" + movingSpeed );	
		}

		while((boolean) ((currentBlock.getY())!=(StadiumGrid.start_y-1))) {
			currentBlock=stadium.moveTowards(currentBlock,x,StadiumGrid.start_y,myLocation);
			//System.out.println("Thread "+this.ID + " swimming " + currentBlock.getX()  + " " +currentBlock.getY() );
			sleep((int) (movingSpeed*swimStroke.strokeTime));  //swim
		}
		
	}
	
	//!!!You do not need to change the method below!!!
	//after finished the race
	public void exitPool() throws InterruptedException {		
		int bench=stadium.getMaxY()-swimStroke.getOrder(); 			 //they line up
		int lane = currentBlock.getX()+1;//slightly offset
		currentBlock=stadium.moveTowards(currentBlock,lane,currentBlock.getY(),myLocation);
	   while (currentBlock.getY()!=bench) {
		 	currentBlock=stadium.moveTowards(currentBlock,lane,bench,myLocation);
			sleep(movingSpeed*3);  //not rushing 
		}
	}
	
	public void run() {
		try {

			//Swimmer arrives
			sleep(movingSpeed+(rand.nextInt(10))); //arriving takes a while
			myLocation.setArrived();
			

			if (swimStroke.order == 1)
			{
				enterStadium();
				MedleySimulation.teams[team].order_strokes[0].countDown(); // Signal Swimstroke 2 that Swimstroke 1 has entered stadium
				goToStartingBlocks();

				try {
					MedleySimulation.boom.await(); // wait until all backstrokers have reached starting position
				} catch (BrokenBarrierException e) {
					e.printStackTrace();
				}
				
				dive();
				swimRace();
				MedleySimulation.teams[team].boom2[0].countDown(); // signal swimstroke 2 that swimstroke 1 finished leg

			}
			else if (swimStroke.order == 2)
			{
				MedleySimulation.teams[team].order_strokes[0].await();  // wait for Swimstroke 1 until entered stadium
				enterStadium();
				MedleySimulation.teams[team].order_strokes[1].countDown(); // Signal Swimstroke 3 that Swimstroke 2 has entered stadium
				goToStartingBlocks();

				
				MedleySimulation.teams[team].boom2[0].await();   // wait for backstroke(swimstroke.order 1) to finish leg
				dive();
				swimRace();
				MedleySimulation.teams[team].boom2[1].countDown(); // signal swimstroke 3 that swimstroke 2 finished leg

			}else if (swimStroke.order == 3)
			{
				MedleySimulation.teams[team].order_strokes[1].await(); // wait for Swimstroke order 2 until entered stadium
				enterStadium();
				MedleySimulation.teams[team].order_strokes[2].countDown(); // Signal Swimstroke 4 that Swimstroke 3 has entered stadium
				goToStartingBlocks();

				
				MedleySimulation.teams[team].boom2[1].await();   // wait for breaststroke(swimstroke.order 2) to finish leg
				dive();
				swimRace();
				MedleySimulation.teams[team].boom2[2].countDown(); // signal swimstroke 4 that swimstroke 3 finished leg
			
			}else if (swimStroke.order == 4)
			{
				MedleySimulation.teams[team].order_strokes[2].await();  // wait for Swimstroke order 3 until entered stadium
				enterStadium();
				goToStartingBlocks();

				
				MedleySimulation.teams[team].boom2[2].await(); // wait for butterfly(swimstroke.order 3) to finish leg 
				dive();
				swimRace();
			}
			
				

			if(swimStroke.order==4) {
				finish.finishRace(ID, team); // fnishline
			}
			else {
				//System.out.println("Thread "+this.ID + " done " + currentBlock.getX()  + " " +currentBlock.getY() );			
				exitPool();//if not last swimmer leave pool
			}
			
		} catch (InterruptedException e1) {  //do nothing
		} 
	}
	
}
