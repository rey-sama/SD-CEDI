package manifest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;
import java.util.StringTokenizer;

import exeptions.TargetNotFoundException;
import struct.dataset.Attribut;
import struct.dataset.Dataset;
import struct.dataset.Transaction;
import struct.subgroup.Interval;
import struct.subgroup.Selector;

public class LastChapter
{
	public static void main(String[] args) throws TargetNotFoundException, IOException, InterruptedException
	{
		f1("AP");
	}

	public static void f1(String dataLab) throws TargetNotFoundException, IOException, InterruptedException
	{
		String sourceFileName = "dataset/"+dataLab+".csv";
		File sourceFile = new File(sourceFileName);
		Dataset data = new Dataset(sourceFile, null, ",");

		int nbTransaction = data.getNbTransaction();

		System.out.println(data.getAttributAt(0));
		System.out.println(data.getAttributAt(1));
		System.out.println(data.getTarget());

		//		for(int i = 0; i < data.getNbAttribut();i++)
		//		{
		//			data.getAttributAt(i).valuesToString();
		//		}

		String resultFileName = "resultFiles/rCEDI"+dataLab+".txt";
		File resultFile = new File(resultFileName);
		Scanner scan = new Scanner(resultFile);
		Calendar date = Calendar.getInstance();
		String sortieLabel= date.get(Calendar.HOUR)+"_"+date.get(Calendar.MINUTE)+"_"+date.get(Calendar.SECOND);
		int conter = 0;
		File sortieGraph = new File("resultsModif/graph"+"_"+sortieLabel);
		sortieGraph.createNewFile();
		FileWriter sortieGraphWriter = new FileWriter(sortieGraph);
		while(scan.hasNext())
		{
			String line = scan.nextLine();
			System.out.println('\n'+line+'\n');
			line = line.replaceAll("]\\[", "] U [");
			System.out.println('\n'+line+'\n');

			System.out.println("FULL COUNTER!!"+conter);
			
			conter++;
			
			if(conter>1000)
			{
				break;
			}
			
			File sortie = new File("resultsModif/"+conter+"_"+sortieLabel);
			sortie.createNewFile();
			FileWriter sortieWriter = new FileWriter(sortie);

			StringTokenizer tokenizer = new StringTokenizer(line, "U: \t");
			ArrayList<Boolean> initiale=new ArrayList<Boolean>();
			ArrayList<Boolean> finale = new ArrayList<Boolean>();
			for(int i = 0; i < nbTransaction; i ++)
			{
				initiale.add(true);
				finale.add(false);
			}

			Attribut attr = null;
			Selector sel=null;

			ArrayList<Selector> selectors = new ArrayList<Selector>();

			parcoursToken:while (tokenizer.hasMoreElements())
			{
				String token = (String) tokenizer.nextElement();
				if(token.matches(".*[a-zA-DF-Z].*") || token.contains("("))
				{
					if(sel!=null)
					{
						ArrayList<Boolean> newFinale = new ArrayList<Boolean>();
						parcoursTransaction:for(int j = 0 ; j < attr.getNbTransaction(); j++)
						{
							newFinale.add(false);
							boolean inSel = false;
							int indexTarget = attr.getTransactionId(j);
							if(!initiale.get(indexTarget))
							{
								continue;
							}
							for(int nbInter = 0 ; nbInter < sel.getNbHole()+1; nbInter++)
							{
								Interval inter = sel.getInterval(nbInter);
								int min = inter.getIndexMin();
								int max = inter.getIndexMax();
								inSel = inSel || (min<=j && j<=max);
								if(inSel)
								{
									finale.set(indexTarget, true);
									continue parcoursTransaction;
								}
							}
						}
						initiale = finale;
						finale = newFinale;
						sortieWriter.write(sel+"\n");
					}

					if(token.contains("("))
					{
						break;
					}

					for(int i = 0; i < data.getNbAttribut();i++)
					{
						Attribut tmpAttr = data.getAttributAt(i);
						if(tmpAttr.getLabel().equals(token))
						{

							attr = tmpAttr;
							sel = new Selector(attr);
							selectors.add(sel);
							continue parcoursToken;
						}
					}
					System.err.println("No " + token);
					System.exit(-1);
				}
				else if(token.matches("^\\[.*"))
				{
					float min = Float.parseFloat(token.replaceAll("\\[", "").replaceAll(";.*", ""));
					float max = Float.parseFloat(token.replaceAll("\\]", "").replaceAll(".*;", ""));
					int indexMin=attr.indexOfValueMin(min);
					int indexMax=attr.indexOfValueMax(max);
					//				System.out.println(indexMin + " : " + min);
					//				System.out.println(indexMax + " : " + max);
					Interval inter = new Interval(attr, indexMin, indexMax, false, false);
					sel.addInterval(inter);
				}
				else
				{
					break;
				}
			}
			sortieWriter.write("\n");
			float sum=0;
			float nb=0;

			for(Selector selec : selectors)
			{
				for(int numInter = 0; numInter <=  selec.getNbHole(); numInter++)
				{
					Interval inter = selec.getInterval(numInter);
					int min = inter.getIndexMin();
					int max = inter.getIndexMax();
					int nbVal = 0;
					for(int numTrans = min; numTrans<=max;numTrans++)
					{
						int transId = selec.attribut.getTransactionId(numTrans);
						if(initiale.get(transId))
						{
							System.out.println(selec.attribut.getValueAt(numTrans));
							nbVal++;
						}
					}
					sortieWriter.write(inter + " :" + nbVal+"\n");
				}
				sortieWriter.write("\n");
			}

			for(int i = 0; i < data.getNbTransaction(); i++)
			{
				if(initiale.get(i))
				{
					Transaction trans = data.getTransactionAt(i);
					nb++;
					sum+=trans.getTargetValue();
				}
			}
			double score = Math.sqrt(nb)*(sum/nb - data.getMeanTarget());
			System.out.println(score);
			sortieWriter.write("Somme val: "+sum + "\nNb Val: "+nb +"\nScore: " + score + "\n");

			int selector = 0;
			for(Selector selec : selectors)
			{
				if(selec.getNbHole()!=0)
				{
					for(int nbInter = 0; nbInter <= selec.getNbHole(); nbInter++)
					{
						ArrayList<Selector> newSelec = (ArrayList<Selector>) selectors.clone();
						ArrayList<Boolean> newInitiale = (ArrayList<Boolean>) initiale.clone();
						float newSum = sum;
						float newNb= nb;


						Selector newSelector = new Selector(selec.attribut);
						for(int nbInter2 = 0; nbInter2 <= selec.getNbHole(); nbInter2++)
						{
							if(nbInter!=nbInter2)
							{
								newSelector.addInterval(selec.getInterval(nbInter2));
							}
							else
							{
								Interval inter = selec.getInterval(nbInter);
								for(int i = inter.getIndexMin(); i <= inter.getIndexMax(); i++)
								{
									int idTrans = inter.getAttribut().getTransactionId(i);
									if(newInitiale.get(idTrans))
									{
										newInitiale.set(idTrans, false);
										Transaction trans = inter.getAttribut().getTransaction(i);
										newSum -= trans.getTargetValue();
										newNb--;
									}
								}
							}
						}
						if(newNb!=0)
						{
							newSelec.set(selector, newSelector);
							sortieWriter.write("\nNew Selector\n");
							for(Selector selec2 : newSelec)
							{
								sortieWriter.write(selec2+"\n");
							}
							double newScore = Math.sqrt(newNb)*(newSum/newNb - data.getMeanTarget());
							sortieWriter.write("Somme val: "+newSum + "\nNb Val: "+newNb +"\nScore: " + newScore + "\n");
							sortieGraphWriter.write((newScore/score)+","+(newNb/nb)+","+""+newScore+","+ score + ","+newNb +"," + nb +"\n");
						}
					}
				}
				selector++;
			}

			sortieWriter.write("-------------------\n");
			
			selector = 0;
			for(Selector selec : selectors)
			{
				if(selec.getNbHole()!=0)
				{
					for(int nbInter = 0; nbInter < selec.getNbHole(); nbInter++)
					{
						ArrayList<Selector> newSelec = (ArrayList<Selector>) selectors.clone();
						ArrayList<Boolean> newInitiale = (ArrayList<Boolean>) initiale.clone();
						float newSum = sum;
						float newNb= nb;


						Selector newSelector = new Selector(selec.attribut);
						for(int nbInter2 = 0; nbInter2 <= selec.getNbHole(); nbInter2++)
						{
							if((nbInter!=nbInter2)&&(nbInter!=nbInter2-1))
							{
								newSelector.addInterval(selec.getInterval(nbInter2));
							}
							else if(nbInter!=nbInter2)
							{
							}
							else
							{
								Interval inter = selec.getInterval(nbInter);
								Interval inter2 = selec.getInterval(nbInter+1);
								int min=inter.getIndexMin();
								int max=inter2.getIndexMax();
								float interMin = inter.getValMax();
								float interMax = inter2.getValMin();
								int indexInterMin = inter.getIndexMax();
								int indexInterMax = inter2.getIndexMin();
								Interval newInter = new Interval(inter.getAttribut(), min, max,false,false);
								newSelector.addInterval(newInter);
								parcoursTrans:for(int j = indexInterMin ; j < indexInterMax; j++)
								{
									Transaction trans = selec.attribut.getTransaction(j);
									boolean resu = true;
									for(Selector select : selectors)
									{
										if(select.attribut.equals(newSelector.attribut))
										{
											continue;
										}
										boolean resu2 = false;
										float tmpVal = trans.getValueAt(select.attribut);
										for(int numInter = 0; numInter <= select.getNbHole(); numInter++)
										{
											Interval tmpInter = select.getInterval(numInter);
											if((tmpInter.getValMin()<=tmpVal)&&(tmpInter.getValMax()>=tmpVal))
											{
												resu2=true;
												break;
											}
										}
										if(!resu2)
										{
											continue parcoursTrans;
										}
									}
									if(resu && !initiale.get(trans.getId()))
									{
										newSum+=trans.getTargetValue();
										newNb++;
										newInitiale.set(trans.getId(), true);
									}
								}
							}
						}
						newSelec.set(selector, newSelector);
						sortieWriter.write("\nNew Selector\n");
						for(Selector selec2 : newSelec)
						{
							sortieWriter.write(selec2+"\n");
						}
						double newScore = Math.sqrt(newNb)*(newSum/newNb - data.getMeanTarget());
						sortieWriter.write("Somme val: "+newSum + "\nNb Val: "+newNb +"\nScore: " + newScore + "\n");
						
						sortieGraphWriter.write((newScore/score)+","+(newNb/nb)+","+""+newScore+","+ score + ","+newNb +"," + nb +"\n");
					}
				}
				selector++;
			}
			sortieWriter.close();
		}
		sortieGraphWriter.close();
		scan.close();
	}

	public static void f2(String dataLab) throws TargetNotFoundException, IOException, InterruptedException
	{
		String sourceFileName = "dataset/"+dataLab+".csv";
		File sourceFile = new File(sourceFileName);
		Dataset data = new Dataset(sourceFile, null, ",");

		int nbTransaction = data.getNbTransaction();

		System.out.println(data.getAttributAt(0));
		System.out.println(data.getAttributAt(1));
		System.out.println(data.getTarget());
		
		String resultFileName = "resultFiles/rCEDI"+dataLab+".txt";
		File resultFile = new File(resultFileName);
		Scanner scan = new Scanner(resultFile);
		Calendar date = Calendar.getInstance();
		String sortieLabel= date.get(Calendar.HOUR)+"_"+date.get(Calendar.MINUTE)+"_"+date.get(Calendar.SECOND);
		int conter = 0;
		File sortieGraph = new File("resultsModif/graph"+"_"+sortieLabel);
		sortieGraph.createNewFile();
		FileWriter sortieGraphWriter = new FileWriter(sortieGraph);
		
		
	}
}
