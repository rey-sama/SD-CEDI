package struct.subgroup;

import java.util.ArrayList;

import struct.dataset.Attribut;
import struct.dataset.Transaction;
import struct.subgroup.utils.ResumErosion;

public class Interval
{
	
	/////////////////////Variable//////////////////
	
	private Attribut attribut;
	private int indexMin;
	private int indexMax;
	private boolean lockLeft;
	private boolean lockRight;
	
	private boolean breakable;
	
	/////////////////////CONSTRUCTORS//////////////////	
	
	private Interval(Attribut attribut, int indexMin, int indexMax, boolean breakable, boolean lockLeft, boolean lockRight)
	{
		super();
		this.attribut = attribut;
		this.indexMin = indexMin;
		this.indexMax = indexMax;
		this.breakable= breakable;
		this.lockLeft = lockLeft;
		this.lockRight= lockRight;
	}
	public Interval(Attribut attribut, int indexMin, int indexMax, boolean lockLeft, boolean lockRight)
	{
		this(attribut, indexMin, indexMax, false, lockLeft, lockRight);
	}
	public Interval(Attribut attribut, int indexMin, int indexMax, boolean breakable)
	{
		this(attribut, indexMin, indexMax, breakable, false, false);
	}
	public Interval(Attribut attribut, int indexMin, int indexMax)
	{
		this(attribut, indexMin, indexMax, false, false, false);
	}
	public Interval(Attribut attribut, boolean breakable)
	{
		this(attribut, 0, attribut.getIndexMax(), breakable, false, false);
	}
	public Interval(Attribut attribut)
	{
		this(attribut, 0, attribut.getIndexMax(), true, false, false);
	}
	
	
	public boolean matches(float value)
	{
		float valMin = getValMin();
		float valMax = getValMax();
		if((valMin <= value) && (value <= valMax))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	///////////////////////UTILS\\\\\\\\\\\\\\\\\\\\\\\\
		//////////////////Erosion\\\\\\\\\\\\\\\\\\\
	public ArrayList<ResumErosion> erosion(Subgroup group)
	{
		ArrayList<ResumErosion> resums = new ArrayList<ResumErosion>();
		
		if(lockLeft && lockRight)
		{
			return resums;
		}
		
		if(lockLeft)
		{
			ResumErosion resum = erosionRight(group);
			
		}
		
		if(lockRight)
		{
			ResumErosion resum = erosionLeft(group);
			
		}
		
		return resums;
	}
	
	public ResumErosion erosionLeft(Subgroup group)
	{
		float valInit = attribut.getValueAt(indexMin);
		float valMax = attribut.getValueAt(indexMax);
		
		if(this.lockLeft || (valInit == valMax) )
		{
			return null;
		}
		else
		{
			int index = indexMin;
			
			int nbDelete = 0;
			ArrayList<Integer> ids = new ArrayList<Integer>();
			
			float currValue = attribut.getValueAt(index);
			
			while(currValue==valInit)
			{
				int idTrans = attribut.getTransactionId(index);
				if(group.isTransactionInHere(idTrans))
				{
					ids.add(idTrans);
					nbDelete++;
				}
				index++;
				currValue = attribut.getValueAt(index);
			}
			
			int transId = attribut.getTransactionId(index);
			while(!group.isTransactionInHere(transId))
			{
				if(index==indexMax)
				{
					return null;
				}
				index++;
				transId = attribut.getTransactionId(index);
			}
			int indexMin = index;
			int indexMax = this.indexMax;
			
			Interval newInterval = new Interval(attribut, indexMin, indexMax, lockLeft, lockRight);
			ResumErosion resume = new ResumErosion(nbDelete, valInit, newInterval, index,ids);
			return resume;
		}
	}
	
	public ResumErosion erosionRight(Subgroup group)
	{
		float valInit = attribut.getValueAt(indexMax);
		float valMin = attribut.getValueAt(indexMin);
		if(this.lockRight || (valInit == valMin) )
		{
			return null;
		}
		else
		{
			int index = indexMax;
			
			int nbDelete = 0;
			ArrayList<Integer> ids = new ArrayList<Integer>();
			
			float currValue = attribut.getValueAt(index);
			
			while(currValue==valInit)
			{
				int idTrans = attribut.getTransactionId(index);
				if(group.isTransactionInHere(idTrans))
				{
					ids.add(idTrans);
					nbDelete++;
				}
				index--;
				currValue = attribut.getValueAt(index);
			}
			
			int transId = attribut.getTransactionId(index);
			while(!group.isTransactionInHere(transId))
			{
				if(index==indexMin)
				{
					return null;
				}
				index--;
				transId = attribut.getTransactionId(index);
			}
			int indexMin = this.indexMin;
			int indexMax = index;
			
			Interval newInterval = new Interval(attribut, indexMin, indexMax, true, false);
			ResumErosion resume = new ResumErosion(nbDelete, valInit, newInterval, index, ids);
			return resume;
		}
	}

	public ResumErosion erosionIntern(int indexInit, Subgroup group)
	{
		float valInit = attribut.getValueAt(indexInit);
		float valMax = attribut.getValueAt(indexMax);
		if(!this.breakable || (valInit == valMax) )
		{
			return null;
		}
		else
		{
			int index = indexInit;
			
			int nbDelete = 0;
			ArrayList<Integer> ids = new ArrayList<Integer>();
			
			float currValue = attribut.getValueAt(index);
			
			while(currValue==valInit)
			{
				int idTrans = attribut.getTransactionId(index);
				if(group.isTransactionInHere(idTrans))
				{
					ids.add(idTrans);
					nbDelete++;
				}
				index++;
				currValue = attribut.getValueAt(index);
			}
			
			int transId = attribut.getTransactionId(index);
			while(!group.isTransactionInHere(transId))
			{
				if(index==indexMax)
				{
					return null;
				}
				index++;
				transId = attribut.getTransactionId(index);
			}
			int indexMin = this.indexMin;
			int indexMax = this.indexMax;
			
			Interval newInterval = new Interval(attribut, indexMin, indexInit-1, false, true);
			Interval newInterval2 = new Interval(attribut, index, indexMax, true);
			ResumErosion resume = new ResumErosion(nbDelete, valInit, newInterval, newInterval2, indexInit-1,index,ids);
			return resume;
		}
	}
	
	//////////////////////TESTER\\\\\\\\\\\\\\\\\\\\\\\
	
	public boolean isIntersect(Interval inter)
	{
		return !((indexMin > inter.indexMax) || (inter.indexMin > indexMax));
	}

	////////////////////GETTER/SETTER\\\\\\\\\\\\\\\\\\\
	
	public int getIndexMin()
	{
		return indexMin;
	}
	public float getValMin()
	{
		return attribut.getTransaction(indexMin).getValueAt(attribut);
	}

	public int getIndexMax()
	{
		return indexMax;
	}
	public float getValMax()
	{
		return attribut.getTransaction(indexMax).getValueAt(attribut);
	}
	
	public boolean isSealed()
	{
		return lockLeft && lockRight;
	}
	public boolean isLockedLeft()
	{
		return lockLeft;
	}
	public boolean isLockedRight()
	{
		return lockRight;
	}
	public boolean isBreakable()
	{
		return breakable;
	}
	
	public Attribut getAttribut()
	{
		return this.attribut;
	}
	
	public String getInterval()
	{
		return getInterval(false);
	}
	public String getInterval(boolean locks)
	{
		return "["+(lockLeft && locks?"*":"")+getValMin()+";"+getValMax()+(lockRight && locks?"*":"")+"]";
	}
	
	public String toString()
	{
		return toString(false);
	}
	public String toString(boolean locks)
	{
		String str = attribut.getLabel()+":"+(breakable?"[":"")+getInterval(locks)+(breakable?"]":"");
		return str;
	}
}
