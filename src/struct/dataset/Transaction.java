package struct.dataset;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class Transaction
{
	private static int ID = 0;
	private int id;
	private ArrayList<Float> values;
	private Float targetValue;
	
	private final Dataset dataset;
	
	public Transaction(Dataset dataset, String line, String separator)
	{
		this.id = ID;
		ID++;
		
		this.dataset=dataset;
		
		values = new ArrayList<Float>();
	
		int indexTarget = dataset.getTarget().getIndexInDataset();
		boolean targetPassed = false;
		int i = 0;
		
		StringTokenizer token = new StringTokenizer(line, separator);
		while (token.hasMoreElements())
		{
			float value = Float.parseFloat(token.nextToken());
			if((indexTarget == i) && !targetPassed)
			{
				targetPassed=true;
				targetValue=value;
			}
			else
			{
				values.add(value);
				i++;
			}
			//System.out.println(attributs.get(i)+""+value);
		}
	}
	
	public int getId()
	{
		return id;
	}

	public float getValueAt(Attribut attribut)
	{
		return getValueAt(attribut.getId());
	}
	public float getValueAt(int idAttribut)
	{
		try
		{
			return values.get(idAttribut);
		}
		catch (IndexOutOfBoundsException e)
		{
			return targetValue;
		}
	}
	
	public float getTargetValue()
	{
		return targetValue;
	}
	
	public String toString()
	{
		String str = "";
		for(int i = 0; i < values.size();i++)
		{
			System.out.println(dataset.getAttributAt(i)+":"+values.get(i));
		}
		System.out.println(dataset.getTarget()+":"+targetValue);
		return str;
	}
}
