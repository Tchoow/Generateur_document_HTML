/** Generateur.java cette classe crée un objet permettant de generer le code HTML et javascript
 * dans les fichiers correspondants
 * date   : 14/01/2021 
 * @author: Equipe 42
 */

import java.io.PrintWriter       ;
import java.io.File              ;
import java.io.FileOutputStream  ;
import java.io.OutputStreamWriter;

public class Generateur
{
	public static void main(String[] args)
	{
		String startCode; // Correspond au début du code HTML
		String header   ; // Correpond à l'en-tête de la page
		String footer   ; // Correspond au pied de page
		String nav      ; // Correspond au menu de navigation
		String article  ; // Correpond à l'aticle de la page

		// Correspond à un petit bloc déstinés à pouvoir retourner sur
		// la page de garde/sommaire/Chapitre précédent suivant
		String aside  ;

		String nameFile  ;
		String numChapter; 
		String prefixe   ;
		String js        ;
		
		File fichier;
		File ficCss ;
		File repJS  ;
		File repAnimation;

		nameFile = "";

		ficCss  = new File(args[1] + "css/" + args[2]);
		fichier = new File(args[0] + "table_ordonancement.data");

		repJS        = new File(args[1] + "js/");
		repAnimation = new File(args[1] + "animations/");

		if ( !fichier.isFile() )
			System.out.println("La table d'ordonnancement n'existe pas.");
		if ( !ficCss.isFile() )
			System.out.println("Le fichier ou le repertoire css n'existe pas.");

		if ( !fichier.exists() || !ficCss.exists() )System.exit(0);;

		EditHTML html = new EditHTML( args[0], args[1] );

		html.editPageGarde( args[0], args[1] );

		try
		{
			header  = html.getHeader (); // Récupère le header final
			footer  = html.getFooter (); // Récupère le footer final
			prefixe = html.getPrefixe(); // Récupère le préfixe des noms de fichiers

		// Boucle dédié à écrire dans les fichiers chapitre
		for (int i=1; i<html.getNbChapitre(); i++)
		{
				numChapter = String.format("%02d", i);
				nameFile = args[1] + prefixe + "_" + numChapter + ".html";

				html.reset();
				html.readFile(i, args[0], args[1]);

				PrintWriter chapitre = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nameFile), "UTF8" ));

				startCode = html.getStartCode( args[1], args[2], i );

				aside   = html.getBtnAside( i, args[1] );
				article   = "\n\t\t<!-- Contenu principale -->\n\t\t<article>";
				article  += html.getArticle ();
				article  += "\n\t\t</article>\n";
				nav       = html.getNav     ();
				js        = html.getJavaScript();

				chapitre.print(startCode);
				chapitre.print(header);
				chapitre.print(aside);
				chapitre.print(nav);
				chapitre.print(article);

				// Etablie un lien javascript que s'il y a du script
				if ( !js.equals(""))
					chapitre.print("\n\t\t<script src=\"" + args[1] + "js/script" + 
					               String.format("%02d", i ) + ".js\"></script>\n");

				chapitre.print(footer);
				chapitre.print("\n\t</body>\n</html>");
				chapitre.close();

				// Créer un fichier javascript que s'il y a du contenu
				if ( !js.equals("") )
				{
					if ( !repAnimation.isDirectory() )
					{
						System.out.println("Le repertoire animations n'existe pas.");
						System.exit(0);
					}
					if ( !repJS.exists() )repJS.mkdir();
					nameFile = args[1] + "js/script" + String.format("%02d", i ) + ".js";
					PrintWriter javaScript = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nameFile), "UTF8" ));
					javaScript.print(js);
					javaScript.close();
				}
			}

			// Partie dédié à la création de la page sommaire
			nameFile = args[1] + prefixe + "_00.html";
			PrintWriter pageSommaire = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nameFile), "UTF8" ));

			startCode = html.getStartCode( args[1], args[2], -1 );

			pageSommaire.print(startCode);
			pageSommaire.print("\t\t<header>\n\t\t\t<p>Sommaire</p>\n\t\t</header>\n\t\t<nav>");
			pageSommaire.print(html.getSommaire());
			pageSommaire.print("\n\t\t\t</ul>\n\t\t</nav>\n\t</body>\n</html>");
			pageSommaire.close();

			// Partie dédiée à la création de la page de garde
			nameFile = args[1] + "suffixe.html";
			PrintWriter pageGarde = new PrintWriter(new OutputStreamWriter(new FileOutputStream(nameFile), "UTF8" ));

			startCode = html.getStartCode( args[1], args[2], 0 );

			pageGarde.print(startCode);
			pageGarde.print(html.getPageGarde());

			pageGarde.print("\n\t\t<nav id=\"navGarde\">\n\t\t\t<p>Sommaire</p>\n\t\t\t" + 
			                html.getSommaire() + "\n\t\t</nav>\n");

			pageGarde.print(html.getArticleGarde());
			pageGarde.print("\n\t</body>\n</html>");
			pageGarde.close();
		}
		catch(Exception e){ e.printStackTrace(); }
	}
}