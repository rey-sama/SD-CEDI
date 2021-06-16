package struct.dataset;

import java.util.ArrayList;

public class Attribut
{
	private int id;
	private static int ID=0;

	private int indexInDataset;

	private String label;
	private ArrayList<Transaction> transactions;

	public Attribut(String label, boolean target)
	{
		super();
		this.label = label;
		this.transactions = new ArrayList<Transaction>();

		if(target)
		{
			this.id=-1;
			indexInDataset = ID;
		}
		else
		{
			this.id=ID;
			ID++;
		}

	}
	public Attribut(String label)
	{
		this(label, false);
	}

	/**
	 * Uses dichotomy to add a transaction in the right place in regards on its value on the attribute 
	 * @param trans : the transaction which had to be added
	 * @return The value of the transaction for the current transaction
	 */
	public float addTransaction(Transaction trans)
	{
		float value = trans.getValueAt(id);
		int indexMin=0;
		int indexMax=transactions.size()-1;
		if(indexMax==-1)
		{
			transactions.add(trans);
			return value;
		}
		float valMin=transactions.get(indexMin).getValueAt(id);
		float valMax=transactions.get(indexMax).getValueAt(id);
		if(valMin>=value)
		{
			transactions.add(0, trans);
			return value;
		}
		if(valMax<=value)
		{
			transactions.add(trans);
			return value;
		}

		while(true)
		{
			int indexMid = (indexMax+indexMin)/2;
			float valMid=transactions.get(indexMid).getValueAt(id);
			if(valMid>value)
			{
				indexMax=indexMid;
				valMax=valMid;
			}
			else if(valMid<value)
			{
				indexMin=indexMid;
				valMin=valMid;
			}
			else
			{
				transactions.add(indexMid, trans);
				return value;
			}
			if((indexMin+1)==indexMax)
			{
				transactions.add(indexMax, trans);
				return value;
			}
		}
	}

	////////////////////////////////GETTER/SETTER///////////////////////////////////

	/**
	 * Return the id of the attribute, which is also its place in the attribute list in the dataset object
	 * @return .id
	 */
	public int getId()
	{
		return id;
	}

	/**
	 * Return the label of the attribute
	 * @return .label
	 */
	public String getLabel()
	{
		return label;
	}

	/**
	 * Return a clone of the ordered list of transaction
	 * @return .transactions (clone)
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<Transaction> getTransactions()
	{
		return (ArrayList<Transaction>) transactions.clone();
	}
	
	public float getValueAt(int index)
	{
		return transactions.get(index).getValueAt(this);
	}
	/**Renvoie la transaction à un indice donné*/
	public Transaction getTransaction(int index)
	{
		return transactions.get(index);
	}

	public int getTransactionId(int index)
	{
		return transactions.get(index).getId();
	}
	/**
	 * Return the number of transactions
	 * @return the number of transactions
	 */
	public int getNbTransaction()
	{
		return transactions.size();
	}
	/**
	 * Return last index of the dataset
	 * @return the number of transactions minus 1
	 */
	public int getIndexMax()
	{
		return transactions.size()-1;
	}
	
	/**Renvoie la valeur minimale de l'attribut*/
	public Float getMin()
	{
		try
		{
			return getTransaction(0).getValueAt(this);
		} catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**Renvoie la valeur maximale de l'attribut*/
	public Float getMax()
	{
		try
		{
			return getTransaction(transactions.size()-1).getValueAt(this);
		} catch (IndexOutOfBoundsException e)
		{
			return null;
		}
	}

	/**
	 * Return the value of the attribut in the dataset file
	 */
	public int getIndexInDataset() {
		return indexInDataset;
	}

	////////////////////////////////UTIL///////////////////////////////////

	public String toString()
	{
		return "("+id+") "+label+": ["+getMin()+";"+getMax()+"]";
	}

	public void valuesToString()
	{
		float value = getMin()-1;
		int i=0;
		for(Transaction trans : transactions)
		{
			if(trans.getValueAt(this)!=value)
			{
				System.out.println(i+ ":" +trans.getValueAt(this));
				value=trans.getValueAt(this);
			}
			i++;
		}
	}
	
	public int indexOfValueMin(float value)
	{
		for(int i = 0; i < getNbTransaction(); i++)
		{
			if(getValueAt(i)==value)
			{
				return i;
			}
		}
		return -1;
	}
	public int indexOfValueMax(float value)
	{
		for(int i = getNbTransaction()-1; i >=0 ; i--)
		{
			if(getValueAt(i)==value)
			{
				return i;
			}
		}
		return -1;
	}
}
