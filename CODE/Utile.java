/** Utile.java cette classe crée un objet permettant d'effectuer dessus des manipumations
 *  utiles pour l'édition du code html.
 * date   : 14/01/2021 
 * @author: Equipe 42
 */

public class Utile
{
	private String curLine;  // Correspond à la ligne actuelle
	private String oldLine;  // Correspond à la ligne précédente 
	private String type   ;  // Correspond au type de la ligne 
	private String oldType;  // Correspond au type de la ligne précédente
	private String prefixe;  // Correspond au préfixe du nom de fichier


	public Utile()
	{
		/*------------------------------*/
		/* Initialisation des attributs */
		/*------------------------------*/

		this.curLine = "";
		this.oldLine = "";
		this.type    = "";
		this.oldType = "";
		this.prefixe = "";
	}

	// Actualise l'ancienne ligne et la ligne actuelle
	public void setLine( String curLine )
	{
		this.oldLine = this.curLine;
		this.curLine = curLine     ; 
	}

	// Actualise le type et l'ancien type à chaque nouvelle ligne, même si type ou ligne sont vides
	public void setType()
	{
		this.oldType = this.type;
		if ( this.curLine.indexOf(":") == 3 ) this.type = this.curLine.substring(0,3);
		else                                  this.type = "";
	}

	// Actualise le préfixe à partir du contenu du fichier déstiné à la page de garde
	public void setPrefixe()
	{
		if ( this.getType().equals("FGA") )this.prefixe = this.getContent();
	}


	// Accesseurs sur attributs
	public String getType       (){ return this.type       ; }
	public String getOldType    (){ return this.oldType    ; }
	public String getCurLine    (){ return this.curLine    ; }
	public String getOldLine    (){ return this.oldLine    ; }
	public String getPrefixe    (){ return this.prefixe    ; }

	// Retourne le contenu de la ligne actuelle du fichier lu dans le scanner en cours de EditHTML
	public String getContent(){ return this.stringReplace();}

	// Remet à la situation initiale les attributs liés au type
	public void resetType()
	{
		this.type    = "";
		this.oldType = "";
	}

	// Passe le type obtenu à la balise correspondante
	public String convertisseur( String type )
	{
		String balise;
		balise = "";
		balise = switch ( type )
		{
			case "CTI"        ->  "h1";
			case "CT1"        ->  "h2";
			case "CT2"        ->  "h3";
			case "CPS","PGA"  ->  "p" ;
			case "CCO"        -> "pre";
			case "CLO"        ->  "li";
			default           ->  ""  ;
		};

		if ( balise.equals("") )
		{
		if ( !this.oldType.equals(this.type) &&
			this.type.equals("CPC"))            // Cas particulier du paragraphe encadré
				balise = "p class=\"cadreP\"";
		else
			balise = "p";
		}
		return balise;
	}

	// Sert a déinir si c'est une balise qui doit être éditer sur une ligne
	public boolean oneLine( String type )
	{
	if ( type.equals("CPS") ||
	     type.equals("CPC") ||
	     type.equals("CCO") ||
	     type.equals("PGA") ||
	     type.equals("C/C") ||
		 type.equals("C\\C"))
		return false;

	return true;
	}

	// Convertie les nombres arabes en nombres romain
	public String arabeToRomain( int arabe )
	{
		String romain;

		romain = "";

		// Interpréte la dizaine
		romain = switch(arabe/10)
		{
			case 0  -> ""     ;
			case 1  -> "X"    ;
			case 2  -> "XX"   ;
			case 3  -> "XXX"  ;
			case 4  -> "XL"   ;
			case 5  -> "L"    ;
			case 6  -> "LX"   ;
			case 7  -> "LXX"  ;
			case 8  -> "LXXX" ;
			case 9  -> "XC"   ;
			default -> ""    ;
		};

		// Interpréte l'unité
		romain += switch(arabe%10)
		{
			case 0  -> ""    ;
			case 1  -> "I"   ;
			case 2  -> "II"  ;
			case 3  -> "III" ;
			case 4  -> "IV"  ;
			case 5  -> "V"   ;
			case 6  -> "VI"  ;
			case 7  -> "VII" ;
			case 8  -> "VIII";
			case 9  -> "IX  ";
			default -> ""    ;
		};
		return(romain);
	}

	// Vérifie s'il s'agit d'un titre
	public boolean estTitre()
	{
		if ( this.type.equals("CT1") ||
		     this.type.equals("CT2") )
		return true;

		return false;
	}

	// Vérifie s'il s'agit d'un chapitre
	public boolean estChapitre(){ return this.type.equals("CTI"); }

	// Permet de remplacer les caractères spéciaux contenue dans la partie de la ligne
	public String stringReplace()
	{
		String sRet;
		sRet = this.curLine.substring(4);

		sRet = ( sRet.replace( "<" , "&lt;" ));
		sRet = ( sRet.replace( ">" , "&gt;" ));
		sRet = ( sRet.replace( "#/G#" , "<b>" ));
		sRet = ( sRet.replace( "#\\G#" , "</b>" ));
		sRet = ( sRet.replace( "#/I#" , "<i>" ));
		sRet = ( sRet.replace( "#\\I#" , "</i>" ));
		sRet = ( sRet.replace( "#CRLF#" , "<br />" ));

		return sRet;
	}
}