package struct.subgroup;

import java.util.ArrayList;
import java.util.Collections;

import struct.dataset.Attribut;
import struct.dataset.Dataset;
import struct.dataset.Transaction;
import struct.subgroup.utils.ResumErosion;

public class Subgroup
{
	public static int NB_HOLE_MAX;
	
	private ArrayList<Selector> selectors;
	
	/**
	 * listOf boolean which indicate if the transaction at the index i is include in the subgroup
	 * for optimisation reason, false -> present and true -> absent
	 */
	private ArrayList<Boolean> listTransaction;
	private int indexTransMin;
	private int indexTransMax;
	
	private float score;
	private int nbElements;
	private float sumTarget;
	public final Dataset dataset;
	public final int nbHoleMax;
	public final int nbHoleTotal;
	
	//TODO fonction calculant le best et renvoyant la valeur minimal utilisée dans le calcul
	private float bestScore;
	
	public Subgroup(Dataset data)
	{
		nbElements = data.getNbTransaction();
		sumTarget = data.getSumTarget();
		score=0;
		indexTransMin=0;
		indexTransMax=nbElements-1;
		listTransaction = new ArrayList<Boolean>(Collections.nCopies(nbElements, true));
		this.dataset = data;
		this.nbHoleMax=0;
		this.nbHoleTotal=0;
		
		selectors = new ArrayList<Selector>();
		for(int i = 0; i < data.getNbAttribut(); i++)
		{
			Attribut attr = data.getAttributAt(i);
			Selector selector = new Selector(attr);
			Interval inter = new Interval(attr);
			selector.addInterval(inter);
			selectors.add(selector);
		}
	}
	
	public Subgroup(ArrayList<Selector> selectors, int nbHoleMax,int nbHoleTotal, ArrayList<Boolean> listTransaction, int indexTransMin, int indexTransMax,
			float score, int nbElements, float sumTarget, float bestScore, Dataset data) {
		super();
		this.selectors = selectors;
		this.listTransaction = listTransaction;
		this.indexTransMin = indexTransMin;
		this.indexTransMax = indexTransMax;
		this.score = score;
		this.nbElements = nbElements;
		this.sumTarget = sumTarget;
		this.bestScore = bestScore;
		this.dataset = data;
		this.nbHoleMax = nbHoleMax;
		this.nbHoleTotal = nbHoleTotal;
	}


	public void testErosion()
	{
		//TODO delete this function
		System.out.println(this.selectors.get(0));
		this.selectors.get(0).erode(this);
	}

	public ArrayList<Subgroup> erosion()
	{
		int indexSelect;
		String keyCombi = "";
		Selector selector=null;
		for(indexSelect = 0; indexSelect < selectors.size();indexSelect++)
		{
			selector = selectors.get(indexSelect);
			keyCombi+=selector.attribut;
			if(!selector.isSealed())
			{
				break;
			}
		}
		ArrayList<Subgroup> listGroup = selector.erode(this);
		if(listGroup.size()!=0)
		{
			Subgroup sub0 = listGroup.get(0);
			if(!dataset.putCombiInfo(sub0.listTransaction, keyCombi))
			{
				listGroup.remove(0);
				System.out.println("------------------REMOVED------------------------");
			}
		}
		return listGroup;
	}

	public boolean isTransactionInHere(int index)
	{
		return listTransaction.get(index);
	}
	
	public float calculBestScore()
	{
		float sumTarget=0;
		int nbElements=0;
		Attribut target = dataset.getTarget();
		float bestScore = 0;
		float score=0;
		for(int i = listTransaction.size()-1; i>=0 ;i--)
		{
			Transaction trans = target.getTransaction(i);
			int index = trans.getId();
			if(isTransactionInHere(index))
			{
				float value = target.getValueAt(i);
				sumTarget+=value;
				nbElements++;
				float mean = sumTarget/nbElements;
				score = (float) Math.pow(nbElements, 0.5)*(mean-dataset.getMeanTarget());
				if(score > bestScore)
				{
					bestScore=score;
				}
			}
		}
		this.bestScore = bestScore;
		this.nbElements = nbElements;
		this.sumTarget = sumTarget;
		this.score=score;
		return bestScore;
	}
	
	public String toString()
	{
		return toString(false);
	}
	public String toString(boolean locks)
	{
		String str = "";
		for(Selector selector : selectors)
		{
			str+=selector.toString(locks)+"\n";
		}
		return str;
	}

	public Subgroup remplace(Selector select, ResumErosion resume)
	{
		ArrayList<Selector> selectors = new ArrayList<Selector>();
		int nbHoleMax = 0;
		int nbHoleTotal = 0;
		for(Selector selector : this.selectors)
		{
			String labelSel1 = select.attribut.getLabel();
			String labelSel2 = selector.attribut.getLabel();
			
			if(!labelSel1.equals(labelSel2))
			{
				selectors.add(selector);
				nbHoleMax = (nbHoleMax>selector.getNbHole()?nbHoleMax:selector.getNbHole());
				nbHoleTotal += selector.getNbHole();
			}
			else
			{
				selectors.add(select);
				nbHoleMax = (nbHoleMax>select.getNbHole()?nbHoleMax:select.getNbHole());
				nbHoleTotal += select.getNbHole();
			}
		}
		
		if(resume==null)
		{
			Subgroup son = new Subgroup(selectors, nbHoleMax, nbHoleTotal, listTransaction, indexTransMin, indexTransMax, score, nbElements, sumTarget, bestScore,dataset);
			son.calculBestScore();
			return son;
		}
		
		ArrayList<Boolean> listTransaction = (ArrayList<Boolean>) this.listTransaction.clone();
		for(Integer id : resume.ids)
		{
			listTransaction.set(id, false);
		}
		
		int indexTransMin= this.indexTransMin;
		int indexTransMax= this.indexTransMax;
		
		while(!listTransaction.get(indexTransMin))
		{
			indexTransMin++;
		}
		
		while(!listTransaction.get(indexTransMax))
		{
			indexTransMax--;
		}
		
		int nbElements =  this.nbElements-resume.nbDelete;
		float sumTarget =  this.sumTarget-resume.nbDelete*resume.valDelete;
		float mean = sumTarget/nbElements;
		
		float score = (float) Math.pow(nbElements, 0.5)*(mean-dataset.getMeanTarget());
		
		Subgroup son = new Subgroup(selectors, nbHoleMax, nbHoleTotal, listTransaction, indexTransMin, indexTransMax, score, nbElements, sumTarget, bestScore,dataset);
		son.calculBestScore();
		return son;
	}

	public float getBestScore()
	{
		return bestScore;
	}
	
	public float getScore()
	{
		return score;
	}
	
	public Dataset getDataset()
	{
		return dataset;
	}
}
