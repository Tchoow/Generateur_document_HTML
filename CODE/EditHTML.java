/** EditHtml.java cette classe crée un objet permettant d'editer au fur et à mesure
 * les différentes parties du code html et javascript de la page à généré.
 * date   : 14/01/2021 
 * @author: Equipe 42
 */

import java.util.Scanner      ;
import java.util.ArrayList    ;
import java.io.FileInputStream;
import java.io.File;

public class EditHTML
{
	private String header ; // Contient l'en-tete de la page
	private String footer ; // Contient le pied de page de la page
	private String nav    ; // Contient le menu de navigation interne de la page sauf page de garde
	private String article; // Contient l'intérieur de la balise article de la page

	private Utile  curLine; // Objet de type Utile, s'actualise à chaque nouvelle ligne

	private ArrayList<String> alNameFile; // Contient les nom de fichiers grace à l'ordonnancement 

	private String nameFilePath; // Chemin d'accès à un fichier

	private String sommaire;     // Correspond au contenu de la page sommaire

	private String startPageGarde; // Correspond au début de la page de garde
	private String articleGarde  ; // Correspond au contenu de la balise article de la page de garde

	private int numTitre1; // Compteur des titres de niveau 1 pour numéroter et faire les ancres
	private int numTitre2; // Compteur des titres de niveau 2 pour numéroter et faire les ancres

	private int     nbDiv    ; // Compte le nombre de balises div dans lequel le code est editer
	private boolean lastDiv  ; // Enregistre si le dernier type existant est une div ou non
	private String  indentDiv; // Contient autant de tabulations qu'il y a de div pour indenter

	private String startJs; // Debut du code javascript avec déclaration de variables
	private String debutJs; // Code permettant de revenir à la première image
	private String preceJs; // Code permettant de revenir à l'image précédente
	private String nextJs ; // Code permettant de passer  à l'image suivante

	private int    nbFile ; // Permet de compter le nombre de fichiers chapitres 

	//Constructeur
	public EditHTML( String src, String racine )
	{
		/*------------------------------*/
		/* Initialisation des attributs */
		/*------------------------------*/

		this.curLine  = new Utile();
		this.header   = "";
		this.footer   = "";
		this.article  = "";
		this.nav      = "";
		this.sommaire = "";

		this.startPageGarde = "";
		this.articleGarde   = "";

		this.numTitre1 = 1;
		this.numTitre2 = 1;

		this.nbDiv     = 0    ;
		this.lastDiv   = false;
		this.indentDiv = ""   ;

		this.nbFile = 0;

		this.startJs = "";
		this.debutJs = "";
		this.preceJs = "";
		this.nextJs  = "";

		this.alNameFile    = new ArrayList<String>();

		readOrdonnancement( src ); // Lit la table d'ordonnacement
	}

	//Permet de lire la table d'ordonnancement
	public void readOrdonnancement(String src)
	{
		String file;
		File   fichier;
		try
		{
			Scanner sc = new Scanner(new FileInputStream( src + "table_ordonancement.data"), "UTF-8" );

			// Lecture du fichier ligne par ligne
			while ( sc.hasNextLine() )
			{
				file = sc.nextLine();
				fichier = new File(src + file);

				if ( !fichier.exists() ) // Vérifie l'existance du fichier
				{
					System.out.println("Le fichier" + fichier + 
									   " n'existe pas, des erreurs risques d'etre produites");
					continue;
				}

				this.alNameFile.add(file); //On récupère le nom du fichier
				this.nbFile++;
			}
			sc.close();
		}
		catch(Exception e){ e.printStackTrace(); }
	}

	// Permet de lire les fichiers chapitres et selon le type d'editer le code HTML et javascript
	public void readFile( int numChapter, String src, String racine )
	{
		boolean changeChapter; // Permet de savoir si l'on change de chapitre ou pas

		changeChapter = true;
		this.nameFilePath = src + this.alNameFile.get(numChapter);

		this.resetNumeration(); // On reinitialise les titres de niveau 1 et 2
		this.resetDiv();        // On réinitialise les variables liés au balises div

		// Initialisation du contenu de la balise nav
		this.nav = "\n\t\t<!-- Menu de navigation -->\n\n\t\t<nav>\n\t\t\t<p>Contenu de la page" +
		           "</p>\n\t\t\t<ul type=\"none\">\n";

		// Initialisation du contenu des différentes parties javascript 
		this.startJs = "var ";
		this.debutJs = "function debut(nom)\n{\n\tvar fic;\n\n\t"         ;
		this.preceJs = "function precedent(nom)\n{\n\tvar fic=\"\";\n\n\t";
		this.nextJs  = "function suivant(nom)\n{\n\tvar fic=\"\";\n\n\t"  ;

		try
		{
			Scanner sc = new Scanner(new FileInputStream(this.nameFilePath), "UTF-8");

			while ( sc.hasNextLine() )
			{
				this.curLine.setLine( sc.nextLine() ); // On actualise l'objet Utile
				this.curLine.setType();                // On actualise le type

				// Si c'est un type de titre ou chapitre alors on l'ajoute au sommaire
				if ( this.curLine.estTitre() || this.curLine.estChapitre() )
				{
					this.editPageSommaire(numChapter, changeChapter);
					changeChapter = false;

					// Si c'est un titre alors on l'ajoute à la nav
					if (this.curLine.estTitre())
						this.nav += editNav();
				}
				
				// On édite le contenu de l'article
				this.article += editArticle(numChapter, racine);

				// Cas où nous sommes à la dernière ligne du fichier
				// Si le type ne représente pas une balise qui se ferme automatiquement
				// Alors on la ferme
				if ( !sc.hasNextLine())
				{
					// Si ce n'est pas un type qui s'écrit sur une ligne, on regarde le type
					if ( !this.curLine.oneLine(this.curLine.getType()))
					{
						if ( this.curLine.getType().equals("CPS") ||
						     this.curLine.getType().equals("CPC"))
							this.article += "\n\t\t</p>\n\n";
					
						if ( this.curLine.getType().equals("CCO"))
							this.article += "\n</pre>\n\n";
					}
					if ( this.curLine.getType().equals("CLO"))
						this.article += "\n\t\t\t</ul>";

				}
			}
			// On ferme les dernières balises du sommaire et du menu de navigation
			this.sommaire += "\t\t\t\t\t</ul>\n\t\t\t\t</ul>";
			this.nav += "\t\t\t\t</ul>\n\t\t\t</ul>\n\t\t</nav>\n";


			
		}
		catch(Exception e){ e.printStackTrace(); }
	}

	// Méthode permettant d'éditer la page de garde
	public void editPageGarde( String src, String racine ) 
	{
		String[] splitCurLine; // Utile dans le cas où le type correspond à une image
		this.articleGarde = "\n\t\t<!-- Article de la page de garde -->" + 
		                    "\n\t\t<article id=\"artGarde\">\n";
		this.nameFilePath = src + this.alNameFile.get(0);
		try
		{
			Scanner sc = new Scanner(new FileInputStream(this.nameFilePath), "UTF-8");
			while ( sc.hasNextLine() )
			{
				curLine.setLine( sc.nextLine() );

				splitCurLine = this.curLine.getCurLine().split(":");

				// Actualise le type et le suffixe
				this.curLine.setType();
				this.curLine.setPrefixe();

				this.header  += editHeader (racine);
				this.footer  += editFooter ();

				// Header unique à la page de garde
				if( this.curLine.getType().equals("TGA") )
					this.startPageGarde += "\t\t<header>\n\t\t\t<h1>" + this.curLine.getContent()
					                    +  "</h1>\n\t\t</header>";

				// Paragraphe contenue dans la page de garde
				if( this.curLine.getType().equals("PGA"))
				{
					if ( !this.curLine.getOldType().equals("PGA") )
						this.articleGarde += "\n\t\t\t<p>\n\t\t\t\t" + this.curLine.getContent();
					else
						this.articleGarde += "\n\t\t\t\t" + this.curLine.getContent() ;
				}

				// Permet de fermer le PGA
				if ( this.curLine.getOldType().equals("PGA") && 
				    !this.curLine.getType().equals("PGA")    ||
				    !sc.hasNextLine()                         )
					this.articleGarde += "\n\t\t\t</p>\n";

				// Si c'est une image de page de garde
				if ( this.curLine.getType().equals("IGA") )
					this.articleGarde += "\n\t\t\t<img class=\"garde\" src=\"" + racine + "images/" +
					                     splitCurLine[1] + "\" alt=\"" + splitCurLine[2] +"\"/>\n";

			}
			// Ferme l'article de la page de garde
			this.articleGarde   += "\t\t</article>";
		}
		catch(Exception e){ e.printStackTrace(); }
	}

	// Permet d'éditer le fichier sommaire
	public void editPageSommaire(int numChapter, boolean changeChapter)
	{
		String numeroChapitre;

		numeroChapitre = String.format("%02d", numChapter);

		// Si c'est un nouveau chapitre
		if ( changeChapter )
		{
			if ( numChapter == 1)this.sommaire +="\n\t\t\t<ul type=\"none\">";

			this.sommaire += "\n\t\t\t\t<li class=\"editChapter\"><a href=\"" + this.getPrefixe() +
			                 "_" + numeroChapitre + ".html\">"                                    +
			                 this.curLine.arabeToRomain(numChapter)+ " "                          +
			                 this.curLine.getContent() + "</a></li>\n\t\t\t\t<ul type=\"none\">";

			this.resetNumeration();
		}

		// Si c'est un titre de niveau 1
		if ( this.curLine.getType().equals("CT1") )
		{
			if ( this.numTitre1 > 1)
				this.sommaire += "\t\t\t\t\t</ul>";

			this.sommaire += "\n\t\t\t\t\t<li><a href=\"" + this.getPrefixe() + "_" +numeroChapitre
			              +  ".html#" + this.numTitre1 +"\">" + this.numTitre1 + " " +
						  this.curLine.getContent() +"</a></li>"+ "\n\t\t\t\t\t<ul type=\"none\">\n";
		}

		// Si c'est un titre de niveau 2
		if ( this.curLine.getType().equals("CT2") )
		{
			this.sommaire += "\t\t\t\t\t\t<li><a href=\"" + this.getPrefixe() + "_" + numeroChapitre +
			                 ".html#" + (this.numTitre1-1) +"." + this.numTitre2 + "\">"             +
			                 (this.numTitre1-1) + "." + this.numTitre2+" "                           +
			                 this.curLine.getContent() + "</a></li>\n";
		}
	}

	// Edite le header des fichiers chapitres selon la partie du type TEN
	public String editHeader( String racine)
	{
		if ( this.curLine.getType().equals("TEN"))
			return "\t\t<header>\n\t\t\t<p>" + curLine.getContent() + "</p>\n\t\t</header>\n";   
		return "";
	}

	// Edite le footer des fichiers chapitres selon la partie du type TPI
	public String editFooter()
	{
		if( this.curLine.getType().equals("TPI") )
			return "\n\t\t<footer>\n\t\t\t<p>"+this.curLine.getContent()+"</p>\n\t\t</footer>\n";
		return "";
	}

	// Edite le menu de navigation de chaque chapitre
	public String editNav()
	{
		String code = "";

		if ( this.curLine.estTitre())
		{
			String content;
			String startTag;
			String endTag;

			startTag = endTag = "";

			// Si c'est un titre de niveau 1 alors on créer une nouvelle liste
			if ( this.curLine.getType().equals("CT1"))
			{
				if ( this.numTitre1 != 1 )startTag = "\t\t\t\t</ul>\n";
				content   =  this.numTitre1 + " " + this.curLine.getContent();
				startTag += "\t\t\t\t<li><a href = \"#" + this.numTitre1 + "\">";
				endTag    = "</a></li>\n\t\t\t\t<ul type=\"none\">\n";
			}
			else // Nécessairement ça ne peut-être que de niveau 2 donc on rajoute un élément
			{
				content  =  (this.numTitre1-1) + "." + this.numTitre2 + " " + this.curLine.getContent();
				startTag = "\t\t\t\t\t<li><a href = \"#" + (this.numTitre1-1) + "." + this.numTitre2 + "\">";
				endTag   = "</a></li>\n";
			}
			code = startTag + content + endTag;
		}
		return code;
	}

	// Edite l'article de la page correspondante
	public String editArticle(int numChapter, String racine)
	{
		String code = "";

		// Verifie si on doit fermer la balise en question
		// Par multiLigne on entend les balises qui tiennent sur plusieurs lignes tel que pre p div
		if ( this.fermerMultiLigne() )
		{
			if ( this.curLine.getOldType().equals("CCO") )
				code += "\n</" + this.curLine.convertisseur(this.curLine.getOldType()) + ">";
			else
				code += "\n\t\t\t"  + this.indentDiv + "</" + 
						this.curLine.convertisseur(this.curLine.getOldType()) + ">";
		}

		// On ouvre la liste
		if ( !this.curLine.getOldType().equals("CLO") && this.curLine.getType().equals("CLO"))
			code += "\n\n\t\t\t" + this.indentDiv + "<ul>";

		// On ferme la liste
		if ( ( this.curLine.getOldType().equals("CLO") && !this.curLine.getType().equals("CLO") ))
			code += "\n\t\t\t" + this.indentDiv + "</ul>";

		// Identifie si on doit ouvrir la balise
		if ( !this.curLine.getType().equals(this.curLine.getOldType()) 
		      && !this.curLine.oneLine(this.curLine.getType()))
		{
			if ( this.curLine.getType().equals("CCO") )
				code += "\n<" + this.curLine.convertisseur(this.curLine.getType()) + ">";
			else
				code += "\n\n\t\t\t" + this.indentDiv + "<" +
				        this.curLine.convertisseur(this.curLine.getType()) + ">";
			
			this.lastDiv = false;
		}

		// Permet la recupération du contenu de la ligne actuelle
		if ( !this.curLine.getType().isEmpty() )
		{
			if ( this.curLine.oneLine( this.curLine.getType() ) )
				code += this.editOneLine( numChapter, racine );
			else
			{
				if ( this.curLine.getType().equals("CCO") )
					code += "\n" + this.curLine.getContent();
				else if ( !this.curLine.getType().equals("C/C") || 
					      !this.curLine.getType().equals("C\\C") )
						code += "\n\t\t\t\t" + this.indentDiv + this.curLine.getContent();
			}
			this.lastDiv = false;
		}

		// Renvoie vers une méthode dédiée à l'animation
		if ( this.curLine.getType().equals("CAN")) code += editAnimation( racine );

		// Renvoie à une méthode dédiée à la gestion des div
		if ( this.curLine.getType().equals("C/C") || this.curLine.getType().equals("C\\C"))
		{
			this.lastDiv = true;
			code = "\n" + this.editDiv();
		}

		// Dans le cas ou nous sommes dans une div nous devons indenter
		this.indentDiv = "";
		for (int i=0; i < this.nbDiv; i++) this.indentDiv += "\t";

		return code;
	}

	// Méthode pour les balises tenant sur une ligne tel que les titres, chapitres etc.
	public String editOneLine(int numChapter, String racine)
	{
		String id     ;
		String code   ;
		String content;
		String balise ;

		boolean bOk = true;

		String[] splitCurLine = this.curLine.getCurLine().split(":"); // Dans le cas des images

		balise = this.curLine.convertisseur(this.curLine.getType());
		code = id = content = "";

		// Selon le type, on écrit le code HTML correspondant
		switch ( this.curLine.getType() )
		{
			case "CTI" -> content =  this.curLine.arabeToRomain(numChapter) + " " + this.curLine.getContent();
			case "CT1" -> 
			{
				content =  this.numTitre1 + " " + this.curLine.getContent();
				id = " id = \"" + Integer.toString(this.numTitre1) + "\"";
				this.numTitre1++;
				this.numTitre2 = 1;
			}
			case "CT2" -> 
			{
				content =  (this.numTitre1-1) + "." + this.numTitre2 + " " + this.curLine.getContent();
				id = " id = \"" + Integer.toString(this.numTitre1-1) + "." + Integer.toString(this.numTitre2) + "\"";
				this.numTitre2++;
			}
			case "CIM" ->
			{
				code  = "\n\t\t\t<img class=\"standard\" src=\"" + racine + "images/" + splitCurLine[1];
				code += "\" alt=\"" + splitCurLine[2]  +"\"/>\n";
			}
			case "TPI", "TEN", "CAN" -> bOk = false; // Passe à faux car ils sont dédiée au footer/header et animations
			default    -> content =  this.curLine.getContent();
		}

		if ( bOk && code.isEmpty())
		{
			if ( this.curLine.getType().equals("CLO")) code = "\n\t\t\t\t" + this.indentDiv;
			else                                       code = "\n\n\t\t\t" + this.indentDiv;

			code += "<" + balise + id + '>' + content + "</" + balise + ">";
		}
		
		return code;
	}

	// Méthode dédiée à l'édition des balises div
	public String editDiv()
	{
		String code;
		code = "";
		if ( !this.curLine.oneLine(this.curLine.getOldType()) )
		{
			// Cas particulier de la balise pre
			if ( this.curLine.getOldType().equals("CCO"))
				code = "</";
			else
				code = "\t\t\t" + this.indentDiv + "</";
					
			code += this.curLine.convertisseur(this.curLine.getOldType()) + ">\n";
		}

		if ( this.curLine.getOldType().equals("CLO") )
			code = "\t\t\t" + this.indentDiv + "</ul>\n";

		if ( this.curLine.getType().equals("C/C") )
		{
			code +=  "\n" + this.indentDiv + "\t\t\t<!-- Bloc -->\n" + 
			         this.indentDiv + "\t\t\t<div class=\"cadreDiv\">";
			this.nbDiv++;
		}
		else
		{
			this.nbDiv--;
			code +=   this.indentDiv + "\t\t</div>\n";
		}
		return code;
	}

	// Met à 1 les attributs liés à la numération des titres
	public void resetNumeration()
	{
		this.numTitre1 = 1;
		this.numTitre2 = 1;
	}

	// Remet à la situation initiale les attributs qui changent à chaque nouveau fichier
	public void reset()
	{
		this.curLine.resetType();
		this.article = "";
		this.nav     = "";
	}

	// Remet à la situation initiale les attributs liés au div
	public void resetDiv()
	{
		this.nbDiv     = 0 ;
		this.indentDiv = "";
	}

	// Méthode permettant de créer le code nécessaire à l'animation
	public String editAnimation( String racine )
	{
		String[] splitCurLine;
		String   ext, nom    ;
		String   ch          ;
		int      nbImage     ;
		String   fin         ;
		String   condition   ;
		
		ch="";
		splitCurLine = this.curLine.getCurLine().split("\\.|\\:");
		nom          = splitCurLine[1];
		ext          = splitCurLine[2];
		nbImage      = Integer.parseInt(splitCurLine[3]);

		// D'abord le code javascript
		fin = ext + "\";" + "\n\t\tdocument.images[nom].src=fic;\n\t}";
		condition = "\n\t\tif ( num" + nom + " < 10 ) s = `0${num" + nom + "}`;" +
		            "\n\t\telse                       s = num";

		if ( !this.startJs.equals("var ")) this.startJs += ",";

		this.startJs += " num" + nom + " = 1";
		this.debutJs += "\n\tif (nom == '" + nom + "')\n\t{\n\t\tnum" + nom + " = 1;" +
		                "\n\t\t\n\t\tfic = \"" + racine + "animations/\" + nom +  \"01."  + fin;

		this.preceJs += "\n\tif (nom == '" + nom + "' && num" + nom + "!=1)\n\t{\n\t\tnum" + nom + 
		                "--;" + condition + nom + ";\n\t\t\n\t\tfic = \"" + racine               + 
		                 "animations/\" + nom + s + \"."  + fin;

		this.nextJs  += "\n\tif (nom == '" + nom + "' && num" + nom + "!=" + nbImage          + 
		                ")\n\t{\n\t\tnum" + nom + "++;" + condition + nom + ";\n\t\tfic = \"" + 
		                racine + "animations/\" + nom + s + \"."  + fin;

		// Puis un tableau dans lequel nous créons les boutons et plaçons l'image
		ch  = "\n\n\t\t\t" + this.indentDiv + "<!--Image animé-->\n\t\t\t" + this.indentDiv     +
		      "<table border=\"1\">\n\t\t\t\t"+this.indentDiv            + 
		      "<tbody>\n"+this.indentDiv +"\t\t\t\t\t<tr>\n"+ this.indentDiv+"\t\t\t\t\t\t<td>\n" +
		      this.indentDiv + "\t\t\t\t\t\t\t<img class=\"anime\" name=\"" + nom + "\" src=\""   +
		      racine + "animations/" + nom + "01." + ext + "\">\n\t\t\t\t\t\t" + this.indentDiv   +
		      "</td>\n\t\t\t\t\t" + this.indentDiv + "</tr>\n" ;

		ch += this.indentDiv + "\t\t\t\t\t<tr>\n" + this.indentDiv + "\t\t\t\t\t\t<td"            + 
		      " align=\"center\">\n\t\t\t\t\t\t\t"  + this.indentDiv + 
		      "<input type=\"button\" name=\"prc\" value=\"|<\" onclick=\"debut('"+ nom +"')\">"  +
		      "\n\t\t\t\t\t\t\t"    + this.indentDiv +
		      "<input type=\"button\" name=\"prc\" value=\"<\" onclick=\"precedent('"+nom+"')\">" +
		      "\n\t\t\t\t\t\t\t"    + this.indentDiv +
		      "<input type=\"button\" name=\"svt\" value=\">\" onclick=\"suivant('"  +nom+"')\">" +
		      "\n\t\t\t\t\t\t"  + this.indentDiv + "</td>\n\t\t\t\t\t"+ this.indentDiv            +
		      "</tr>\n\t\t\t\t"+ this.indentDiv + "</tbody>\n\t\t\t" + this.indentDiv+"</table>\n";

		return ch;
	}

	// Créer le code pour la partie permettant de nous rediriger vers
	// la page d'accueil, le sommaire, le chapitre précédent/suivant et retourne ce code
	public String getBtnAside(int numChapter, String racine )
	{
		String sRet;

		sRet = "\t\t<aside>\n\t\t\t<p>\n\t\t\t\t<a href=\"" + racine + 
		       "suffixe.html\">Accueil</a><br />\n\t\t\t\t<a href=\"" 
		       + racine + this.getPrefixe() + "_00.html\">Sommaire</a>\n\t\t\t</p>";

		if ( numChapter > 1)
			sRet += "\n\t\t\t<p class=\"next\">\n\t\t\t\t<a href=\"" + this.getPrefixe() + "_" +
			        String.format("%02d", numChapter-1)+".html\">Chapitre précédent</a>\n\t\t\t</p>";
		if ( numChapter < this.nbFile-1 )
			sRet +=  "\n\t\t\t<p class=\"next\">\n\t\t\t\t<a href=\"" + this.getPrefixe() + "_" +
			         String.format("%02d", numChapter+1)+".html\">Chapitre suivant</a>\n\t\t\t</p>";

		sRet += "\n\t\t</aside>\n";

		return sRet;
	}

	// Vérifie si on doit fermer une ligne de type multiligne(cf ligne 316 du code de cette classe)
	public boolean fermerMultiLigne()
	{
		if ( !this.curLine.getOldType().isEmpty()                       &&
		     (this.curLine.getCurLine().isEmpty()                       ||
		     !this.curLine.getOldType().equals(this.curLine.getType())) &&
		     !this.curLine.oneLine ( this.curLine.getOldType())         &&
		     !this.curLine.estTitre()                                   &&
		     !this.curLine.getType().equals("C/C")                      &&
			 !this.curLine.getType().equals("C\\C")                     &&
			 !this.lastDiv                                               )
			return true;


		return false;
	}

	// Accesseurs sur différents attributs et code nécessaire a la génération du code HTML
	public int    getNbChapitre (){ return this.alNameFile.size(); }
	public String getHeader     (){ return this.header ; }
	public String getFooter     (){ return this.footer ; }
	public String getArticle    (){ return this.article; }
	public String getNav        (){ return this.nav    ; }

	public String getPrefixe     (){ return this.curLine.getPrefixe(); }
	public String getSommaire    (){ return this.sommaire            ; }
	public String getPageGarde   (){ return this.startPageGarde      ; }
	public String getArticleGarde(){ return this.articleGarde        ; }

	// Place dans une String l'ensemble du code javascript et le retourne 
	public String getJavaScript  ()
	{
		String js;

		if ( this.startJs.equals("var "))
			return "";

		this.startJs += ";\n";
		this.debutJs += "\n}\n";
		this.preceJs += "\n}\n";
		this.nextJs  += "\n}\n";

		js = this.startJs + this.debutJs + this.preceJs + this.nextJs;

		return js;
	}

	// Permet d'adapter le début du code selon la page
	public String getStartCode( String racine, String css, int id )
	{
		String startCode;
		String title   ;

		title = "sommaire";
		if ( id == 0 ) title = "Page de garde " + this.getPrefixe() + "_00";
		if ( id > 0 )  title = this.getPrefixe() + "_" + String.format("%02d", id); 
		startCode  = "<!DOCTYPE html>\n<html lang=\"fr\">\n\t<head>\n\t\t<title>" 
		             +  title + "</title>"+"\n\t\t<meta charset=\"utf-8\" />\n\t\t"
		             + "<link rel=\"stylesheet\" type=\"text/css\" href=\"" + racine + "css/"
		             + css + "\" />\n\t</head>\n\t<body>\n";

		return startCode;
	}
}