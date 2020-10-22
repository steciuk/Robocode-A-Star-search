package fnl;

import java.util.Arrays;

public class State {
	private State parent;
	private int[] pos;
	private int g;
	private int h;
	private int f;
	
	//constructor for start
	public State(int rowP, int colP)
	{
		this.pos = new int[2];
		this.pos[0] = rowP;
		this.pos[1] = colP;
		this.g = 0;
	}
	
	public State(State parent, int[] pos)
	{
		this.pos = new int[2];
		this.pos[0] = pos[0];
		this.pos[1] = pos[1];
		this.parent = parent;
	}

	public boolean isEqual(State s)
	{
		if(Arrays.equals(this.pos, s.getPos()))
			return true;
		else 
			return false;			
	}
	
	public int[] getPos()
	{
		return this.pos;
	}
	
	public State getParent()
	{
		return this.parent;
	}
	
	public int getF()
	{
		return this.f;
	}
	
	public int getG()
	{
		return this.g;
	}
	
	public int getH()
	{
		return this.h;
	}
	
	public void updateH(State end)
	{
		this.h = Math.abs(this.pos[0] - end.pos[0]) + Math.abs(this.pos[1] - end.pos[1]);
		// this.h = (int) (Math.pow(this.pos[0] - end.pos[0], 2) + Math.pow(this.pos[1] - end.pos[1], 2));
	}
	
	private void updateG()
	{
		this.g = this.parent.getG() + 1;
	}
	
	public void updateF()
	{
		this.f = this.g + this.h;
	}
	
	public void updateHGF(State end)
	{
		this.updateH(end);
		this.updateG();
		this.updateF();
	}
}
