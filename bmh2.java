// Paul Chaffanet
// Nom du programme: BoyerMooreHorspool.java
// CHAP23049307
// Question 1 du Devoir 3

// java bmh2 geneX.fasta GGGCT 

import java.io.*;
import java.util.*;

public class bmh2 {

	public static void main(String[] args){
		BufferedReader entree = null;
		String ligne = null;
		String gene = "";
		String mot = null;
		
		// On vérifie que le nombre d'arguments entrés lors de l'exécution du programme soit exactement de deux.
		// Le premier argument passé en paramètre doit être un fichier .txt contenant la séquence d'un gène.
		// Le deuxieme argument passé en paramètre doit être un motif à chercher dans la séquence
		if (args.length != 2) {
			System.out.println("Ne contient pas le bon nombre d'arguments.");
			System.exit(1);
		}
		
		// Le mot recherché doit avoir une longueur d'au moins deux caractères puisque que nous travaillons avec des 2-caracteres
		if(args[1].length() < 2 || !checkStrContainsOnlyLetters(args[1])) {
			System.out.println("Le mot recherché doit avoir au moins deux caractères et ne doit contenir que des lettres");
			System.exit(1);
		}
		
		// On crée un buffer à partir du fichier entré en argument.
		// Si un problème survient lors de l'instanciation du buffer (e.g. un fichier au mauvais format), on explique l'erreur survenue
		// et on quitte le programme.
		int pb = 0;
		try {
			entree = new BufferedReader(new FileReader(args[0]));
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Le fichier ne peut être lu.");
			pb = 1;
		}
		finally {
			if (entree != null && pb  == 1) {
				try {
					entree.close();
				}
				catch (IOException e) {
					e.printStackTrace();
					System.out.println("Une erreur est survenue lors de la fermeture du buffer.");
					System.exit(1);
				}
			}
		}
		
		// On lit les lignes du fichier .txt les unes après les autres (en sautant la première ligne qui ne fait pas partie d'une séquence de gène)
		try {
			boolean premiereLigne = true;
			while((ligne = entree.readLine()) != null) {
				if (premiereLigne && ligne.substring(0,1).equals(">")) {
					premiereLigne = false;
					continue;
				}
				gene = gene.concat(ligne);
			}	
		}
		catch (IOException e) {
			e.printStackTrace();
			System.out.println("Un problème est survenu lors de la lecture du fichier. Le fichier doit être en format .txt");
			System.exit(1);
		}
		finally {
			if (entree != null) {
				try {
					entree.close();
				}
				catch (IOException e) {
					e.printStackTrace();
					System.out.println("Une erreur est survenue lors de la fermeture du buffer.");
				}
			}
		}
		
		// Après avoir lu toutes les lignes du fichier, on effectue une vérification finale que le gene est au bon format
		if (!checkStrContainsOnlyLetters(gene)) {
			System.out.println("Le gène ne peut être lu. Assurez-vous que le fichier est au bon format");
			System.exit(1);
		}
		else {
			gene = gene.toUpperCase();
			mot = args[1].toUpperCase();
			System.out.println("Gène lu: " + gene);
			System.out.println("Mot lu: " + mot);
		}
		
		int longueurMot = mot.length();
		int longueurGene = gene.length();
		
		// Phase de prétraitement du mot.
		// On commence par decomposer le mot en deux caracteres par deux caracteres de la droite vers la gauche
		// A chaque fois qu'un 2-caracteres est lu, on vérifie que l'on ne l'a pas déjà vu en regardant dans la HashMap
		// Si on ne l'a pas vu, on l'ajoute à la hashmap et on associe au 2-caractere sa position dans le mot.
		// Si on l'a déjà vu, alors la nouvelle occurrence de ce 2-caractere se trouvera trop à gauche par rapport à une première
		// occurrence que l'on a vu plus à droite, donc on ne fait rien.
		// Cette étape s'effectue en O(m) où m est la taille du mot/de la séquence recherchée.
		int borneGauche;
		int borneDroite;
		String caracteres;
		Map<String, Integer> decalages = new HashMap<String, Integer>();
		
		for (int i = 0; i < longueurMot - 2; i++) {
			borneGauche = longueurMot - i - 3;
			borneDroite = longueurMot - i - 1;
			caracteres = mot.substring(borneGauche, borneDroite);
			if (decalages.get(caracteres) == null) {
				decalages.put(caracteres, i + 1);
			}
		}
		// Fin du prétraitement du mot

		// Notre pointeur de mot se déplacera lui sur toute la longueur du mot recherché
		int pointeurMot;
		// Ce tableau retiendra en mémoire les positions de toutes les occurrences du mot recherché.
		ArrayList<Integer> tabPointeursOccurrences = new ArrayList<Integer>();
		// Cette variable intermédiaire va nous permettre de stocker la valeur de saut nécessaire pour faire avancer notre pointeur de gene.
		// Le pointeur de gene sera a chaque tour de boucle incrémenté de la valeur du saut.
		Integer saut;
		
		// Itération sur le gene, donc en O(n - m).
		// On place notre pointeur de gene sur la position la plus à droite par rapport au mot afin de conserver notre position dans le gene.
		// Le pointeur de gene se deplacera sur toute la longueur du gene entre les positions m et n.
		// Il s'incrémentera d'un saut calculé à chaque itération.
		for(int pointeurGene = longueurMot - 1; pointeurGene < longueurGene; pointeurGene += saut) {
			// On commence à comparer de la droite vers la gauche en initialisant le pointeur à la fin du mot recherché
			pointeurMot = longueurMot - 1;
			// Tant que l'on a pas atteint le début du mot et que les caracteres comparés de la droite vers la gauche sont égaux, on décrémente notre pointeur de mot afin d'avancer de la droite vers la gauche.
			for (;pointeurMot >= 0 && mot.substring(pointeurMot, pointeurMot + 1).equals(gene.substring(pointeurGene - longueurMot +  1 + pointeurMot, pointeurGene - longueurMot + pointeurMot + 2)); pointeurMot--);
			
			// Si le pointeur de mot a une valeur négative, cela signifie que nous avons trouver une occurrence du mot recherché dans le gene.
			if (pointeurMot < 0)
				// On ajoute alors cette position à la liste des positions des occurrences.
				tabPointeursOccurrences.add(pointeurGene - longueurMot + 1);
			
			// Puis on cherche la position de l'occurrence du 2-caracteres suivant;
			caracteres = gene.substring(pointeurGene - 1, pointeurGene + 1);
			saut = decalages.get(caracteres);
			
			if (saut == null) { // Si pas d'occurrence du 2-car suivant, alors voir si le premier caractère du mot est égal au dernier caractere du 2-car
				// Si oui, alors avancer le premier caractère du mot sous le dernier caractere du 2-car
				if (mot.substring(0,1).equals(caracteres.substring(1,2)))
					saut = longueurMot - 1;
				else
					// Sinon on peut sauter de toute la longueur du mot après le derniere caractere du 2-car pour recommencer la comparaison dans une autre itération.
					saut = longueurMot;
			}
		}
		
		System.out.println("Occurrences: " + tabPointeursOccurrences.size());
		if (tabPointeursOccurrences.size() != 0) {
			System.out.print("Positions: ");
			Iterator<Integer> iterateur = tabPointeursOccurrences.iterator();
			
			while (iterateur.hasNext())
				System.out.print(iterateur.next() + " ");
				
		}
	}
	
	
	
	
	
	/**
	 * Cette fonction permet de vérifier si une chaîne de caractères ne contient que des lettres.
	 * @param str
	 * @return 
	 * Retourne true si la fonction contient que des lettres, false sinon.
	 */
	public static boolean checkStrContainsOnlyLetters(String str) {
		 char[] chars = str.toCharArray();

		    for (char c : chars) {
		        if(!Character.isLetter(c)) {
		            return false;
		        }
		    }
		    return true;
	}
}


