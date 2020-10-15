import java.util.Random;
import java.util.*;

public class CreateRandom {

	//Picks random number between x integers
	public int RandomParam(int... peersx) {

		ArrayList<Integer> intList = new ArrayList<Integer>(0);

		for(int x : peersx) {
			intList.add(x);
		}

		Random rand = new Random();

		int randomElement = intList.get(rand.nextInt(intList.size()));

		return randomElement;

	}


	public int RandomList(List<Integer> PassList) {

		int randomInt = PassList.get(new Random().nextInt(PassList.size()));

		return randomInt;

	}

}