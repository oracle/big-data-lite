package oracle.demo.oow.bd.ui;

import java.util.HashMap;
import java.util.List;

import java.util.Random;
import java.util.Set;

import oracle.demo.oow.bd.dao.GenreDAO;
import oracle.demo.oow.bd.dao.MovieDAO;
import oracle.demo.oow.bd.to.CastTO;
import oracle.demo.oow.bd.to.MovieTO;

public class Test {
  public Test() {
    super();
  }

  public static void main(String[] args) {
    HashMap hm = new HashMap();
    hm.put(10138, "vM81xWzJAmg");     //Iron Man 2
    hm.put(568, "nEl0NsYn1fU");       //Apollo 13
    hm.put(13448, "bcE8QaKiTGk");     //Angels & Demons
    hm.put(10193, "roADdYWAv4A");     //Toy Story 3
    hm.put(10136, "K44VfaWppLI");     //Big //TODO
    hm.put(857, "zwhP5b4tD6g");       //Saving Private Ryan
    hm.put(10191, "Uh0Nb_DPNWk");     //How to Train Your Dragon
    hm.put(10315, "n2igjYFojUo");     //Fantastic Mr. Fox
    hm.put(180, "QH-6UImAP7c");       //Minority Report
    hm.put(330, "NAsME4Wtt6w");       //The Lost World: Jurassic Park
    
    String youtubeKey = (String)hm.get(1);
    if (youtubeKey != null) System.out.println(youtubeKey);
    else {
      Random generator = new Random();
      Object[] values = hm.values().toArray();
      youtubeKey = (String)values[generator.nextInt(values.length)];
    }
  }
}