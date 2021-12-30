package FinalProjectGlascock;

import java.util.LinkedList;
import java.util.Collections;

public class Deck
{
	private LinkedList<Card> cards;
	
	// only for use in accessing cards images from folder
	private final String[] values = {"2", "3", "4", "5", "6", "7", "8", "9", "10", "jack", "queen", "king", "ace"};
	private final String[] suits = {"spades", "clubs", "diamonds", "hearts"};
	
	// initialize the deck and shuffle them all
	public Deck()
	{
		cards = new LinkedList<Card>();
		for(int i = 0; i < values.length; i++)
		{
			for(int j = 0; j < suits.length; j++)
			{
				cards.add(new Card(values[i], suits[j], i));
			}
		}
		
		Collections.shuffle(cards);
	}
	
	// clone constructor to avoid re-initializing all those cards
	public Deck(Deck d)
	{
		cards = new LinkedList<Card>();
		for(int i = 0; i < d.cards.size(); i++)
		{
			cards.add(d.cards.get(i));
		}
		
		Collections.shuffle(cards);
	}
	
	// return the front card then move it to the back
	public Card deal()
	{
		cards.addLast(cards.removeFirst());
		return cards.peekLast();
	}
	
	public void shuffle()
	{
		Collections.shuffle(cards);
	}
}
