package tp1.logic;

import java.util.ArrayList;
import java.util.List;

import tp1.logic.gameobjects.GameObject;

public class GameObjectContainer {
	private List<GameObject> objects;

	public GameObjectContainer() {
		objects = new ArrayList<GameObject>();
	}
	
	// Only one add method (polymorphism)
	public void add(GameObject object) {
	//TODO fill your code
	}
	
	public String postitionToString(Position pos) {
		//TODO fill your code
		return "";
	}

	//TODO fill your code

	// TODO you should write a toString method to return the string that represents the object status
	// @Override
	// public String toString()
}
