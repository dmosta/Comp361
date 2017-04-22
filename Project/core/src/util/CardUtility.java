package util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.ui.Image;

import cards.Card;
import cards.PoliticsCard;
import cards.ScienceCard;
import cards.TradeCard;

public class CardUtility {
	
	private static Image imgAlchemist, imgCrane, imgEngineer, imgInventor, imgIrrigation, imgMedicine, imgMining,
		imgPrinter, imgRoadBuilding, imgSmith, imgBishop, imgConstitution, imgDeserter, imgDiplomat, imgIntrigue,
		imgSaboteur, imgSpy, imgWarlord, imgWedding, imgCommercialHarbor, imgMasterMerchant, imgMerchant, imgMerchantFleet,
		imgResourceMonopoly, imgTradeMonopoly;
	
	public static void loadCards(){
		imgAlchemist=new Image(new Texture(Gdx.files.internal("cards/alchemist.png")));
		imgCrane=new Image(new Texture(Gdx.files.internal("cards/crane.png")));
		imgEngineer=new Image(new Texture(Gdx.files.internal("cards/engineer.png")));
		imgInventor=new Image(new Texture(Gdx.files.internal("cards/inventor.png")));
		imgIrrigation=new Image(new Texture(Gdx.files.internal("cards/irrigation.png")));
		imgMedicine=new Image(new Texture(Gdx.files.internal("cards/medicine.png")));
		imgMining=new Image(new Texture(Gdx.files.internal("cards/mining.png")));
		imgPrinter=new Image(new Texture(Gdx.files.internal("cards/printer.png")));
		imgRoadBuilding=new Image(new Texture(Gdx.files.internal("cards/road_building.png")));
		imgSmith=new Image(new Texture(Gdx.files.internal("cards/smith.png")));
		imgBishop=new Image(new Texture(Gdx.files.internal("cards/bishop.png")));
		imgConstitution=new Image(new Texture(Gdx.files.internal("cards/constitution.png")));
		imgDeserter=new Image(new Texture(Gdx.files.internal("cards/deserter.png")));
		imgDiplomat=new Image(new Texture(Gdx.files.internal("cards/diplomat.png")));
		imgIntrigue=new Image(new Texture(Gdx.files.internal("cards/intrigue.png")));
		imgSaboteur=new Image(new Texture(Gdx.files.internal("cards/saboteur.png")));
		imgSpy=new Image(new Texture(Gdx.files.internal("cards/spy.png")));
		imgWarlord=new Image(new Texture(Gdx.files.internal("cards/warlord.png")));
		imgWedding=new Image(new Texture(Gdx.files.internal("cards/wedding.png")));
		imgCommercialHarbor=new Image(new Texture(Gdx.files.internal("cards/commercial_harbor.png")));
		imgMasterMerchant=new Image(new Texture(Gdx.files.internal("cards/master_merchant.png")));
		imgMerchant=new Image(new Texture(Gdx.files.internal("cards/merchant.png")));
		imgMerchantFleet=new Image(new Texture(Gdx.files.internal("cards/merchant_fleet.png")));
		imgResourceMonopoly=new Image(new Texture(Gdx.files.internal("cards/resource_monopoly.png")));
		imgTradeMonopoly=new Image(new Texture(Gdx.files.internal("cards/trade_monopoly.png")));
	}
	
	public static Card getCardFromString(String str){
		Card card=null;
		boolean valid=false;
		try{
			card=PoliticsCard.valueOf(str);
			valid=true;
		}catch(Exception e){}
		if(!valid){
			try{
				card=TradeCard.valueOf(str);
				valid=true;
			}catch(Exception e){}
		}
		if(!valid){
			try{
				card=ScienceCard.valueOf(str);
				valid=true;
			}catch(Exception e){}
		}
		return card;
	}
	
	public static Image getImage(Card card){
		if(card==ScienceCard.ALCHEMIST)return imgAlchemist;
		if(card==ScienceCard.CRANE)return imgCrane;
		if(card==ScienceCard.ENGINEER)return imgEngineer;
		if(card==ScienceCard.INVENTOR)return imgInventor;
		if(card==ScienceCard.IRRIGATION)return imgIrrigation;
		if(card==ScienceCard.MEDICINE)return imgMedicine;
		if(card==ScienceCard.MINING)return imgMining;
		if(card==ScienceCard.PRINTER)return imgPrinter;
		if(card==ScienceCard.ROAD_BUILDING)return imgRoadBuilding;
		if(card==ScienceCard.SMITH)return imgSmith;
		if(card==PoliticsCard.BISHOP)return imgBishop;
		if(card==PoliticsCard.CONSTITUTION)return imgConstitution;
		if(card==PoliticsCard.DESERTER)return imgDeserter;
		if(card==PoliticsCard.DIPLOMAT)return imgDiplomat;
		if(card==PoliticsCard.INTRIGUE)return imgIntrigue;
		if(card==PoliticsCard.SABOTEUR)return imgSaboteur;
		if(card==PoliticsCard.SPY)return imgSpy;
		if(card==PoliticsCard.WARLORD)return imgWarlord;
		if(card==PoliticsCard.WEDDING)return imgWedding;
		if(card==TradeCard.COMMERCIAL_HARBOR)return imgCommercialHarbor;
		if(card==TradeCard.MASTER_MERCHANT)return imgMasterMerchant;
		if(card==TradeCard.MERCHANT)return imgMerchant;
		if(card==TradeCard.MERCHANT_FLEET)return imgMerchantFleet;
		if(card==TradeCard.RESOURCE_MONOPOLY)return imgResourceMonopoly;
		if(card==TradeCard.TRADE_MONOPOLY)return imgTradeMonopoly;
		return null;
	}
	
}
