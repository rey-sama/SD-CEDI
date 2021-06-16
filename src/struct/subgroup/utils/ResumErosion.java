package struct.subgroup.utils;

import java.util.ArrayList;

import struct.subgroup.Interval;

public class ResumErosion 
{
	public final int nbDelete;
	public final float valDelete;
	public final int index;
	public final int index2;
	public final ArrayList<Integer> ids;
	public final Interval interval1;
	public final Interval interval2;
	public boolean breaked;

	public ResumErosion(int nbDelete, float valDelete, Interval interval, int index, ArrayList<Integer> ids)
	{
		this(nbDelete, valDelete, interval,interval, index, index,ids);
		this.breaked = false;
	}

	public ResumErosion(int nbDelete, float valDelete, Interval interval1, Interval interval2, int index, int index2, ArrayList<Integer> ids)
	{
		this.nbDelete = nbDelete;
		this.valDelete = valDelete;
		this.interval1 = interval1;
		this.interval2 = interval2;
		this.index = index;
		this.index2 = index2;
		this.ids = ids;
		this.breaked=true;
	}

	@Override
	public String toString()
	{
		String str = "Number of deleted item: "+nbDelete;
		str+= "\nValue deleted: " + valDelete;
		str+= "\nValue of the indexes: " + index+ " & " + index2;
		str+= "\nId of the transactions:";
		for(int i : ids)
		{
			str+=" "+i;
		}
		return str;
	}
}
