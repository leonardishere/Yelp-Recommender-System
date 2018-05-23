package yelp_recommender_system;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * KeyMap is a translation layer that helps to use an array as a dictionary.
 * It does this by converting a key into the associated index. The index can then be used in array accesses.
 * 
 * For example, if you wanted the following dictionary: {"key1": "val1", "key2": "val2"}.
 * Then it can be stored as String[] arr = {"val1", "val2"}.
 * Constructing a KeyMap and add the keys "key1" and "key2".
 * Access with arr[map.convert("key1")].
 * 
 * All operations occur in O(1) time, as opposed to O(num keys) with the naive approach.
 * 
 * @author Team 2
 *
 */
public class KeyMap {
	//used to translate keys to indices
	private HashMap<String, Integer> map;
	//used to translate indices to keys
	private ArrayList<String> list;
	
	/**
	 * Constructs an empty KeyMap.
	 */
	public KeyMap() {
		map = new HashMap<>();
		list = new ArrayList<>();
	}
	
	/**
	 * Returns true if the key is contained in this map.
	 * @param key the key to check
	 * @return true if contained, false otherwise
	 */
	public boolean contains(String key) {
		return map.containsKey(key);
	}
	
	/**
	 * Returns true if the index is contained in this map.
	 * @param index the index to check
	 * @return true if contained, false otherwise
	 */
	public boolean contains(int index) {
		return 0 <= index && index < list.size();
	}
	
	/**
	 * Converts the key to its associated index.
	 * Returns -1 if the key is not contained.
	 * @param key the key to convert
	 * @return the index or -1
	 */
	public int convert(String key) {
		if(contains(key)) return map.get(key);
		return -1;
	}
	
	/**
	 * Converts the index to its associated key.
	 * Returns the empty string "" if the index is not contained.
	 * @param index the index to convert
	 * @return the key or the empty string
	 */
	public String convert(int index) {
		if(contains(index)) return list.get(index);
		return "";
	}
	
	/**
	 * Adds the key into this map and returns its index.
	 * Returns -1 if the key is already contained.
	 * @param key the key to add
	 * @return the index or -1
	 */
	public int add(String key) {
		if(contains(key)) return -1;
		int index = list.size();
		map.put(key, index);
		list.add(key);
		return index;
	}
	
	/**
	 * Returns the number of keys currently being stored.
	 * @return the number of keys
	 */
	public int size() {
		return list.size();
	}
	
	/*
	//test
	public static void main(String[] args) {
		System.out.println("in keymap test");
		KeyMap map = new KeyMap();
		System.out.println(map.contains(""));
		System.out.println(map.contains(0));
		map.add("qwer");
		map.add("asdf");
		map.add("zxcv");
		System.out.println(map.convert("qwer"));
		System.out.println(map.convert("asdf"));
		System.out.println(map.convert("zxcv"));
		System.out.println(map.convert(""));
		System.out.println("_"+map.convert(0));
		System.out.println("_"+map.convert(1));
		System.out.println("_"+map.convert(2));
		System.out.println("_"+map.convert(3));
	}
	*/
}
