package fnl;

import java.util.Random;
import java.util.Vector;

import robocode.Robot;
import robocode.ScannedRobotEvent;

public class FnlRobot extends Robot {
	public void run()
	{
		State state = calculatePath();		
		if(state != null)
		{
			System.out.print("go");
			Vector<State> path = new Vector<State>();
			while(state.getParent() != null)
			{
				path.add(state);
				state = state.getParent();
			}
			path.add(state);
			
			for(int i = path.size()-1; i > 0; i--)
			{
				int nextDir = getNextDir(path.get(i), path.get(i-1));
				int currentDir =  (int)(Math.round(getHeading()/90));
				
				if(Math.abs(nextDir - currentDir) == 2)
					turnRight(180);
				else if(nextDir - currentDir == 1 || (nextDir == 0 && currentDir == 3))
					turnRight(90);
				else if(nextDir - currentDir == -1 || (nextDir == 3 && currentDir == 0))
					turnLeft(90);
				
				ahead(64);				
			}			
		}
	}
	
	// 0-up
	// 1-right
	// 2-down
	// 3-left
	
	public int getNextDir(State current, State next)
	{
		int col = next.getPos()[0] - current.getPos()[0];
		int row = next.getPos()[1] - current.getPos()[1];
		
		if(col == 1)
			return 0;
		if(col == -1)
			return 2;
		if(row == 1)
			return 1;
		if(row == -1)
			return 3;
		
		return -1;		
	}
	
	public State calculatePath()
	{
		//SETTINGS
		int seed = 3;		
		int cols = 14;
		int rows = 14;
		double prcntg = 0.2;
		//SETTINGS
		
		Random rng = new Random(seed);
			
		int numObstacles = 0;
		
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
			System.out.println("No way from start to end was found!");
			return null;
		}
		
		System.out.println("Path has been found!");
		return current;
	}
}

