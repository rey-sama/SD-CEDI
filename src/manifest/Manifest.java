package manifest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.sound.midi.SysexMessage;

import exeptions.TargetNotFoundException;
import struct.dataset.Attribut;
import struct.dataset.Dataset;
import struct.subgroup.Interval;
import struct.subgroup.Selector;
import struct.subgroup.Subgroup;

public class Manifest
{

	/**
	 * @param dataset
	 * @param -t target (default, last column)
	 * @param -s separator (default coma <,>)
	 * @throws IOException 
	 * @throws TargetNotFoundException 
	 */

	public static void main(String[] args) throws IOException, TargetNotFoundException
	{
		String filename="";
		int nbHoleMaxSelector = 1;
		int nbHoleMaxSubgroup = 100;
		if(args.length==0)
		{
			System.err.println("Wrong number of argument.\nExpected format: DATASET [nbDisSel] [nbDisSubG]\n"
					+ "With nbDisSel: The maximum number of disontinuity in a selector\n"
					+ "and nbDisSubG: The maximum number of disontinuity in the subgroup.");
			System.exit(-1);
		}
		else
		{
			filename=args[0];
			if(args.length!=1)
			{
				if(args.length==2)
				{
					nbHoleMaxSelector = Integer.parseInt(args[1]);
				}
				else
				{
					nbHoleMaxSelector = Integer.parseInt(args[1]);
					nbHoleMaxSubgroup = Integer.parseInt(args[2]);
				}
			}
		}
		File f = new File(filename);
		Dataset data = new Dataset(f, null, ",");

		Selector.NB_HOLE_MAX=nbHoleMaxSelector;
		Subgroup.NB_HOLE_MAX=nbHoleMaxSubgroup;

		Attribut attr = data.getAttributAt(0);
//		for(int i = 0; i < data.getNbTransaction();i++)
//		{
//			System.out.println(i + ": " + attr.getValueAt(i));
//		}

		Interval inter;
		Selector select = new Selector(attr);

		inter = new Interval(attr, 0, 6,true, true);
		select.addInterval(inter);

		inter = new Interval(attr, 11, 13,true,false);
		select.addInterval(inter);

		//System.out.println(select.toString(true));

		Subgroup group = new Subgroup(data);
		group.calculBestScore();
		ArrayList<Subgroup> toBeTreated = new ArrayList<Subgroup>();
		//ArrayList<Subgroup> toBeTreatedHole = new ArrayList<Subgroup>();
		toBeTreated.add(group);

		while(toBeTreated.size()!=0)
		{
			Subgroup current = toBeTreated.get(0);
			data.updateBest(current);
			toBeTreated.remove(0);
			ArrayList<Subgroup> subgroups = current.erosion();
			for(int i = 0; i < subgroups.size(); i++)
			{
				Subgroup newSubgroup = subgroups.get(i);
				if(data.getBestScore()<newSubgroup.getBestScore())
				{
					dichotoAdd(newSubgroup, toBeTreated);
					//toBeTreated.add(i,subgroups.get(i));
				}
			}
			//System.out.println("Added: "+current.toString(true));

			//System.out.println(toBeTreated.size()+ " " + data.getBestScore()+ " " + current.getBestScore());
		}

		System.out.println(data.getBestSubgroup());
		System.out.println("\nScore: " + data.getBestScore());

		//System.out.println(group.toString(true));

		//group.testErosion();
	}


	//*
	public static void dichotoAdd(Subgroup newEntry,ArrayList<Subgroup> toBeTreated)
	{
		float value = newEntry.getBestScore();
		int indexValMin=toBeTreated.size()-1;
		int indexValMax=0;
		if(indexValMin==-1)
		{
			toBeTreated.add(newEntry);
			return;
		}
		float valMin=toBeTreated.get(indexValMin).getBestScore();
		float valMax=toBeTreated.get(indexValMax).getBestScore();
		if(valMin>=value)
		{
			toBeTreated.add(newEntry);
			return;
		}
		if(valMax<=value)
		{
			toBeTreated.add(0,newEntry);
			return;
		}

		while(true)
		{
			int indexMid = (indexValMax+indexValMin)/2;
			float valMid=toBeTreated.get(indexMid).getBestScore();
			if(valMid>value)
			{
				indexValMax=indexMid;
				valMax=valMid;
			}
			else if(valMid<value)
			{
				indexValMin=indexMid;
				valMin=valMid;
			}
			else
			{
				toBeTreated.add(indexMid, newEntry);
				return;
			}
			if((indexValMin-1)==indexValMax)
			{
				toBeTreated.add(indexValMax, newEntry);
				return;
			}
		}

	}
	//*/
}
