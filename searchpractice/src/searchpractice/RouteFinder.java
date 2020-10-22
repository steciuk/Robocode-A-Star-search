package searchpractice;


import java.util.Random;
import java.util.Vector;

import searchpractice.State;
import robocode.control.*;

import robocode.control.events.*; 

public class RouteFinder {
	public static void main(String[] args)
	{
		 RobocodeEngine engine =  new RobocodeEngine(new java.io.File("C:/Robocode")); 
		 engine.setVisible(true); 
		 	
	 //SETTINGS		 
			int seed = 3;		
			int cols = 14;
			int rows = 14;
			double prcntg = 0.2;
	 //
		 
		 int NumPixelRows = rows * 64;
		 int NumPixelCols = cols * 64;
		 int numObstacles = 0;
		 	 	 
		 BattlefieldSpecification battlefield = new BattlefieldSpecification(NumPixelRows, NumPixelCols);
		 Random rng = new Random(seed);
		 		 
		 int numberOfRounds = 1;
		 long inactivityTime = 10000000;
		 double gunCoolingRate = 1.0;
		 int sentryBorderSize = 50;
		 boolean hideEnemyNames = false; 
		 
		 //generate obstacle map
		int[][] map = new int[rows][cols];
		
		for(int i = 0; i < rows; i++)
		{
			for(int j = 0; j < cols; j++)
			{
				if(rng.nextDouble() < prcntg)
				{
					map[i][j] = 1;
					numObstacles ++;
				}				
			}
		}
		
		// starting position
		int rowT;
		int colT;
		do
		{
			rowT = rng.nextInt(rows);
			colT = rng.nextInt(cols);
		}while(map[rowT][colT] != 0);		
		State start = new State(rowT, colT);
		
		// ending position
		do
		{
			rowT = rng.nextInt(rows);
			colT = rng.nextInt(cols);
		}while(map[rowT][colT] != 0);
		State end = new State(rowT, colT);
		 		 
		 RobotSpecification[] modelRobots = engine.getLocalRepository("sample.SittingDuck, fnl.FnlRobot*"); 
		 RobotSpecification[] existingRobots = new RobotSpecification[numObstacles + 1]; 
		 RobotSetup[] robotSetups = new RobotSetup[numObstacles+1]; 
		 
	 
		 int robIndex = 0;
		 for(int i = 0; i < rows; i++)
		 {
			 for(int j = 0; j < cols; j++)
			 {
				 if(map[i][j] == 1)
				 {
					 double initialObstacleRow = 32 + (i*64);
					 double initialObstacleCol = 32 + (j*64);
					 existingRobots[robIndex] = modelRobots[0];
					 robotSetups[robIndex] = new RobotSetup(initialObstacleCol, initialObstacleRow, 0.0);
					 robIndex++;
				 }
				 
			 }
		 } 
		 
		 existingRobots[numObstacles] = modelRobots[1]; 
		 double initialAgentRow = 32 + (start.getPos()[0] * 64);
		 double initialAgentCol = 32 + (start.getPos()[1] * 64);
		 robotSetups[numObstacles] = new RobotSetup(initialAgentCol, initialAgentRow,0.0);
		 
		 System.out.println("Start: " + start.getPos()[0] + " " + start.getPos()[1]);
		 System.out.println("End: " + end.getPos()[0] + " " + end.getPos()[1]);
		 System.out.println("Obstacle map: ");
		 printMap(map, rows, cols);
		 
		 
		 BattleSpecification battleSpec = new BattleSpecification
				 (
				 battlefield,
				 numberOfRounds,
				 inactivityTime,
				 gunCoolingRate,
				 sentryBorderSize,
				 hideEnemyNames,
				 existingRobots,
				 robotSetups
				 ); 
		 
		 showState(start, end, map, rows, cols);
		 engine.runBattle(battleSpec, true);
		 		 
		 engine.close();
		 
		 System.exit(0); 
	}
	
	public static void showState(State start, State end, int[][] map, int rows, int cols)
	{
		Vector<State> openSet = new Vector<State>();
		Vector<State> closedSet = new Vector<State>();		
		openSet.add(start);
		
		int currentIndex = 0;
		State current = openSet.get(0);
		boolean goalFound = false;
		
		while(!openSet.isEmpty())
		{
			current = openSet.get(0);
			current.updateH(end);
			current.updateF();
			currentIndex = 0;		
			
			for(int i = 0; i < openSet.size(); i++)
			{				
				if(openSet.get(i).getF() < current.getF())
				{
					current = openSet.get(i);
					currentIndex = i;
				}
			}			
		
			openSet.remove(currentIndex);
			closedSet.add(current);
			
			// found goal
			if(current.isEqual(end))
			{
				goalFound = true;
				break;
			}

			
			// create children list
			Vector<State> children = new Vector<State>();
			int[][] dir = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};
			for(int[] d : dir)
			{
				int[] potentialChildPos = {current.getPos()[0] + d[0], current.getPos()[1] + d[1]};
				
				// adjoining node to high or to low
				if(potentialChildPos[0] < 0 || potentialChildPos[0] >= rows)
					continue;
				
				// adjoining node to far to the left or right
				if(potentialChildPos[1] < 0 || potentialChildPos[1] >= cols)
					continue;
				
				// wall
				if(map[potentialChildPos[0]][potentialChildPos[1]] == 1)
					continue;
				
				children.add(new State(current, potentialChildPos));			
			}
				
			// children loop
			for(int i = 0; i < children.size(); i++)
			{
				State curChl = children.get(i);
				
				// already in closed set
				boolean cont = false;
				for(int j = 0; j < closedSet.size(); j++)
				{
					if(curChl.isEqual(closedSet.get(j)))
						cont = true;
				}
				if(cont)
					continue;
			
				curChl.updateHGF(end);
								
				// already is in open set and g is higher
				for(int j = 0; j < openSet.size(); j++)
				{
					if(curChl.isEqual(openSet.get(j)) && curChl.getG() > openSet.get(j).getG())
						cont = true;
				}
				
				if(cont)
					continue;
				
				openSet.add(curChl);
			}
		}
		
		if(!goalFound)
		{
			System.out.println("No path from start to end was found!");
			return;
		}
		
		System.out.println("Path has been found!");
		drawPath(current, map, rows, cols);
	}
	
	public static void drawPath(State s, int[][] map, int rows, int cols)
	{
		map[s.getPos()[0]][s.getPos()[1]] = 9;
		while(s.getParent() != null)
		{
			s = s.getParent();
			map[s.getPos()[0]][s.getPos()[1]] = 7;
		}
		map[s.getPos()[0]][s.getPos()[1]] = 6;
		
		printMap(map, rows, cols);	
	}
	
	public static void printMap(int[][] map, int rows, int cols)
	{
		for(int i = rows - 1; i >= 0; i--)
		{
			for(int j = 0; j < cols; j++)
			{
				System.out.print(map[i][j] + " ");
			}
			System.out.print("\n");
		}
		
		System.out.print("\n");
	}
}
