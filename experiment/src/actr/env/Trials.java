package actr.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
/**
 * This class creates all the required data for the different trials and represents it as an easy-to-use List.
 * Currently each element contains <nBack_level (0-4), construction (true/false)> with a total of 10 elements,
 * conditions are randomized in such a way that the same n-back level isn't driven twice in a row.
 * 
 * @author Milan de Mooij
 */
public class Trials {
    List<Condition> nBack_list = new ArrayList<>(); 
	public Trials() {
        nBack_list.add(new Condition(0, true));
        nBack_list.add(new Condition(1, true));
        nBack_list.add(new Condition(2, true));
        nBack_list.add(new Condition(3, true));
        nBack_list.add(new Condition(4, true));
        nBack_list.add(new Condition(0, false));
        nBack_list.add(new Condition(1, false));
        nBack_list.add(new Condition(2, false));
        nBack_list.add(new Condition(3, false));
        nBack_list.add(new Condition(4, false));
        //goodShuffle(); // XXX Trials are not shuffled!
        // Add first 10 trials in reverse order.
        for(int i = 9; i >= 0; i--)
        {
            nBack_list.add(nBack_list.get(i));
        }
        
    }

    public List<Condition> getList()
    {
        return nBack_list;
    }

    // Till randomization condition is satisfied, continue shuffling.
    public void goodShuffle(){
        Collections.shuffle(nBack_list);
        while(!check(nBack_list) || !check2(nBack_list))
        {
            Collections.shuffle(nBack_list);
        }
    }

    // This method checks if the randomization for n-back is satisfactory.
    public boolean check(List<Condition> list)
    {
        for(int i = 0; i < list.size() - 1; i++)
        {
            if(list.get(i).nBack == list.get(i+1).nBack)
            {
                return false;
            }
        }
        return true;
    }

    public boolean check2(List<Condition> list)
    {
        for(int i = 0; i < list.size() - 1; i++)
        {
            if(list.get(i).construction == list.get(i+1).construction)
            {
                return false;
            }
        }
        return true;
    }
}