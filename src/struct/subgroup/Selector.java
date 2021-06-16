package struct.subgroup;

import java.util.ArrayList;

import struct.dataset.Attribut;
import struct.subgroup.utils.ResumErosion;

public class Selector
{
	public static int NB_HOLE_MAX;
	
	public final Attribut attribut;
	private ArrayList<Interval> intervals;
	private Boolean sealed = null;
	
	public Selector(Attribut attr)
	{
		this.attribut=attr;
		intervals = new ArrayList<Interval>();
	}

	
	
	public boolean addInterval(Interval interval)
	{
		/**
		 * Check if the attribute of the selector equals the attribute of the attribute on the interval
		 */
		if(interval.getAttribut()!=attribut)
		{
			return false;
		}
		try
		{
			if(interval.getIndexMax()<intervals.get(0).getIndexMin())
			{
				intervals.add(0,interval);
				return true;
			}
		}
		catch (IndexOutOfBoundsException e)
		{
			intervals.add(interval);
			return true;
		}

		int indexMax=intervals.size()-1;
		if(interval.getIndexMin()>intervals.get(indexMax).getIndexMax())
		{
			intervals.add(interval);
			return true;
		}

		for(int i = 0; i < indexMax;i++)
		{
			Interval prev = intervals.get(i);
			Interval next = intervals.get(i+1);
			
			if(prev.getIndexMin()>interval.getIndexMax())
			{
				continue;
			}
			else
			{
				if( (prev.getIndexMax()<interval.getIndexMin()) && (next.getIndexMin() > interval.getIndexMax()) )
				{
					intervals.add(i,interval);
					return true;
				}
				return false;
			}
		}
		return false;
	}	
	
	public ArrayList<Subgroup> erode(Subgroup group)
	{
		ArrayList<Subgroup> groups= new ArrayList<Subgroup>();
		
		Interval inter=null;
		int index;
		for(index = 0; index < intervals.size(); index++)
		{
			inter = intervals.get(index);
			if(!inter.isSealed())
			{
				break;
			}
		}
		if(index==intervals.size())
		{
			return groups;
		}
		
		//*
		if(!inter.isLockedLeft() && !inter.isLockedRight())
		{
			groups.addAll(erodeNoLock(index, inter, group));
		}
		else if(inter.isLockedLeft())
		{
			groups.addAll(erodeLockLeft(index, inter, group));
		}
		else
		{
			groups.addAll(erodeLockRight(index, inter, group));
		}
		//*/
		
		if((inter.isBreakable()) && (getNbHole()<NB_HOLE_MAX) && (group.nbHoleTotal<Subgroup.NB_HOLE_MAX))
		{
			groups.addAll(erodeLockIntern(index, inter, group));
		}
		
		return groups;
	}

	private ArrayList<Subgroup> erodeNoLock(int index, Interval inter, Subgroup group)
	{
		ArrayList<Subgroup> groups = new ArrayList<Subgroup>();
		
		ResumErosion resumeLeft = inter.erosionLeft(group);
		ResumErosion resumeRight = inter.erosionRight(group);
		
		Selector selectAllLock = new Selector(this.attribut);
		Selector selectEroLeft= new Selector(this.attribut);
		Selector selectEroRight = new Selector(this.attribut);
		
		for(int j = 0; j < intervals.size();j++)
		{
			Interval interval = intervals.get(j);
			if(j!=index)
			{
				selectAllLock.addInterval(interval);
				selectEroLeft.addInterval(interval);
				selectEroRight.addInterval(interval);
			}
			else
			{
				Interval locked = new Interval(attribut, interval.getIndexMin(), interval.getIndexMax(), true,true);
				selectAllLock.addInterval(locked);
				
				if(resumeLeft!=null)
				{
					selectEroLeft.addInterval(resumeLeft.interval1);
					if(resumeLeft.breaked)
					{
						selectEroLeft.addInterval(resumeLeft.interval2);
					}
				}
				
				if(resumeRight!=null)
				{
					selectEroRight.addInterval(resumeRight.interval1);
					if(resumeRight.breaked)
					{
						selectEroRight.addInterval(resumeRight.interval2);
					}
				}
			}
		}
		
		
		Subgroup subgroup = group.remplace(selectAllLock, null);
		groups.add(subgroup);
		
		if(resumeRight!=null)
		{
			subgroup = group.remplace(selectEroRight, resumeRight);
			groups.add(subgroup);
		}
		
		if(resumeLeft!=null)
		{
			subgroup = group.remplace(selectEroLeft, resumeLeft);
			groups.add(subgroup);
		}
		
		return groups;
	}
	private ArrayList<Subgroup> erodeLockLeft(int index, Interval inter, Subgroup group)
	{
		ArrayList<Subgroup> groups = new ArrayList<Subgroup>();
		
		ResumErosion resume = inter.erosionRight(group);
		Selector selectAllLock = new Selector(this.attribut);
		Selector selectEroRight = new Selector(this.attribut);
		for(int j = 0; j < intervals.size();j++)
		{
			Interval interval = intervals.get(j);
			if(j!=index)
			{
				selectEroRight.addInterval(interval);
				selectAllLock.addInterval(interval);
			}
			else
			{
				Interval locked = new Interval(attribut, interval.getIndexMin(), interval.getIndexMax(), true,true);
				selectAllLock.addInterval(locked);
				
				if(resume!=null)
				{
					selectEroRight.addInterval(resume.interval1);
					if(resume.breaked)
					{
						selectEroRight.addInterval(resume.interval2);
					}
				}
			}
		}
		
		
		Subgroup subgroup = group.remplace(selectAllLock, null);
		groups.add(subgroup);
		
		if(resume!=null)
		{
			subgroup = group.remplace(selectEroRight, resume);
			groups.add(subgroup);
		}
		
		return groups;
	}
	private ArrayList<Subgroup> erodeLockRight(int index, Interval inter, Subgroup group)
	{
		ArrayList<Subgroup> groups = new ArrayList<Subgroup>();

		ResumErosion resume = inter.erosionLeft(group);
		Selector selectAllLock = new Selector(this.attribut);
		Selector selectEroLeft = new Selector(this.attribut);
		for(int j = 0; j < intervals.size();j++)
		{
			Interval interval = intervals.get(j);
			if(j!=index)
			{
				selectEroLeft.addInterval(interval);
				selectAllLock.addInterval(interval);
			}
			else
			{
				Interval locked = new Interval(attribut, interval.getIndexMin(), interval.getIndexMax(), true,true);
				selectAllLock.addInterval(locked);
				
				if(resume!=null)
				{
					selectEroLeft.addInterval(resume.interval1);
					if(resume.breaked)
					{
						selectEroLeft.addInterval(resume.interval2);
					}
				}
			}
		}
		
		
		Subgroup subgroup = group.remplace(selectAllLock, null);
		groups.add(subgroup);
		
		if(resume!=null)
		{
			subgroup = group.remplace(selectEroLeft, resume);
			groups.add(subgroup);
		}

		return groups;
	}

	private ArrayList<Subgroup> erodeLockIntern(int index, Interval inter, Subgroup group)
	{
		ArrayList<Subgroup> groups = new ArrayList<Subgroup>();

		ResumErosion resumMin;
		int indexInit;
		
		ResumErosion resumMax;
		int indexEnd;
		try
		{
			resumMin = inter.erosionLeft(group);
			indexInit = resumMin.index;
			resumMax = inter.erosionRight(group);
			indexEnd = resumMax.index;
		}
		catch (NullPointerException e)
		{
			return groups;
		}
		
		
		
		while(indexInit!=indexEnd)
		{
			ResumErosion resume = inter.erosionIntern(indexInit, group);
//			System.out.println(resume.interval1.toString(true));
//			System.out.println(resume.interval2.toString(true));
//			System.out.println();
			if(resume!=null)
			{
				Selector selectEroInter = new Selector(this.attribut);
				for(int j = 0; j < intervals.size();j++)
				{
					Interval interval = intervals.get(j);
					if(j!=index)
					{
						selectEroInter.addInterval(interval);
					}
					else
					{
						selectEroInter.addInterval(resume.interval1);
						selectEroInter.addInterval(resume.interval2);
					}
				}
				Subgroup subgroup = group.remplace(selectEroInter, resume);
				groups.add(subgroup);
				indexInit=resume.index2;
			}
			else
			{
				break;
			}
			
		}
		
		return groups;
	}
	
	public boolean isSealed()
	{
		if(sealed==null)
		{
			sealed = true;
			for(Interval inter : intervals)
			{
				sealed = sealed && inter.isSealed();
			}
		}
		return sealed;
	}

	public int getNbHole()
	{
		return intervals.size()-1;
	}
	
	public String toString()
	{
		return toString(false);
	}

	public String toString(boolean locks)
	{
		String str = attribut.getLabel()+ ": ";
		for(Interval inter : intervals)
		{
			str+=inter.getInterval(locks);
		}
		return str;
	}

	public Interval getInterval(int nbInter)
	{
		return intervals.get(nbInter);
	}
}
