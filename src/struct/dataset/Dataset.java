package struct.dataset;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.StringTokenizer;

import exeptions.TargetNotFoundException;
import struct.subgroup.Subgroup;

public class Dataset
{
	private ArrayList<Attribut> attributs;
	private Attribut target;
	private ArrayList<Transaction> transactions;
	
	private final float sumTarget;
	private final float meanTarget;
	
	private Subgroup bestSubgroup;
	private float bestScore;
	
	private final Hashtable<ArrayList<Boolean>, ArrayList<String>> combiInfo;
	
	/**
	 * Constructor
	 * @param f : File of the dataset
	 * @param target : Labe of the target variable
	 * @param separator : separator of the csv/tsv file
	 * @throws FileNotFoundException
	 * @throws TargetNotFoundException 
	 */
	public Dataset(File f, String target, String separator) throws FileNotFoundException, TargetNotFoundException
	{
		attributs = new ArrayList<Attribut>();
		transactions = new ArrayList<Transaction>();
		
		Scanner scan = new Scanner(f);
		String head = scan.next();
		//System.out.println(head);
		createHeader(head, target,separator);
		this.sumTarget = convertTransaction(scan, separator);
		this.meanTarget = getSumTarget()/getNbTransaction();
		
		bestSubgroup=null;
		bestScore=0;
		
		combiInfo = new Hashtable<ArrayList<Boolean>, ArrayList<String>>();
	}

	/**
	 * Create the list of attribute with the first line of the file
	 * @param str : first line of the file
	 * @param target : name of the target variable
	 * @param separator : separator : separator of the csv/tsv file
	 * @throws TargetNotFoundException 
	 */
	private void createHeader(String str, String target, String separator) throws TargetNotFoundException
	{
		StringTokenizer token = new StringTokenizer(str, separator);
		String attribut = token.nextToken();
		while (token.hasMoreElements())
		{	
			Attribut attr = createAttributFromLabel(attribut, target);
			attribut = (String)token.nextToken();
		}
		Attribut attr = createAttributFromLabel(attribut, target==null?attribut:target);
		if(this.target==null)
		{
			throw new TargetNotFoundException("There is no attribute \""+target+"\"");
		}
	}
	
	/**
	 * Create an attribute from the parameters
	 * @param label : name of the current variable
	 * @param target : name of the target variable
	 * @return
	 */
	private Attribut createAttributFromLabel(String label, String target)
	{
		Attribut attr;
		if(label.equals(target))
		{
			attr = new Attribut(label, true);
			this.target = attr;
		}
		else
		{
			attr = new Attribut(label);
			attributs.add(attr);
		}
		return attr;
	}

	/**
	 * 
	 * @param scan: Scanner starting from the second line of the file, a.k.a the first line of data
	 * @param separator : separator of the csv/tsv file
	 * @return The sum of the target values
	 */
	private final float convertTransaction(Scanner scan, String separator)
	{
		int indexTarget = target.getIndexInDataset();
		float sumTarget = 0f;
		while (scan.hasNext())
		{
			String line = scan.next();
			Transaction trans = new Transaction(this, line, separator);
			transactions.add(trans);
			for(Attribut attr : attributs)
			{
				attr.addTransaction(trans);
			}
			sumTarget+=target.addTransaction(trans);
		}
		return sumTarget;
	}
	
	
	////////////////////////////////GETTER/SETTER
	
	public Attribut getTarget()
	{
		return target;
	}
	
	public Attribut getAttributAt(int i)
	{
		return attributs.get(i);
	}
	
	public int getNbAttribut()
	{
		return attributs.size();
	}
	
	public float getSumTarget()
	{
		return sumTarget;
	}
	public float getMeanTarget()
	{
		return meanTarget;
	}
	
	/**
	 * Return the number of transactions
	 * @return transactions.size();
	 */
	public int getNbTransaction()
	{
		return transactions.size();
	}
	
	public Transaction getTransactionAt(int index)
	{
		return transactions.get(index);
	}
	
	public float getBestScore()
	{
		return bestScore;
	}
	public Subgroup getBestSubgroup()
	{
		return bestSubgroup;
	}
	
	public void updateBest(Subgroup sub)
	{
		//System.out.println(sub.getScore());
		if(sub.getScore()>bestScore)
		{
			bestSubgroup = sub;
			bestScore = sub.getScore();
		}
	}
	
	public ArrayList<String> getCombiInfo(ArrayList<Boolean> key)
	{
		return combiInfo.get(key);
	}
	
	public boolean putCombiInfo(ArrayList<Boolean> key, String value)
	{
		ArrayList<String> list = combiInfo.get(key);
		if(list==null)
		{
			list = new ArrayList<String>();
			list.add(value);
			combiInfo.put(key, list);
			return true;
		}
		else if(list.contains(value))
		{
			return false;
		}
		else
		{
			list.add(value);
			return true;
		}
	}
}
