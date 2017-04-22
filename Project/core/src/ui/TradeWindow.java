package ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;

import actions.TurnAction;
import comp361.catan.City;
import comp361.catan.Edge;
import comp361.catan.Harbor;
import comp361.catan.Map;
import comp361.catan.Player;
import comp361.catan.Resource;
import comp361.catan.ResourceType;
import comp361.catan.Settlement;
import comp361.catan.Skins;
import comp361.catan.Tile;

/**
 * This window allows the selection of the number of resources offered and wanted in a trade.
 */
public class TradeWindow extends Window{
	
	private HashMap<ResourceType, Integer> resources;
	private HashMap<ResourceType, ResourceTable> resourceTables=new HashMap<ResourceType, ResourceTable>();
	private TextButton buttonSendOffer;
	private TextButton buttonCancel;
	private TextButton buttonAccept;
	private TextButton buttonRefuse;
	private boolean isAnswer;
	private boolean isBankTrade=false;
	private int totalTake=0, totalGive=0;
	private HorizontalGroup group;
	private HashMap<ResourceType, Integer> bestRate=new HashMap<ResourceType, Integer>();
	private HashMap<ResourceType, String> rateInformation=new HashMap<ResourceType, String>();
	private Map map;
	private Player player;
	private Label labelRemaining;
	private ResourceTable goldTable;
	
	public TradeWindow(Player player, Map map){
		super("Trade", Skins.METAL);
		this.map=map;
		this.player=player;
		calculateBestRate();
		resources=player.getResources();
		resourceTables.put(ResourceType.LUMBER, new ResourceTable(ResourceType.LUMBER, false));
		resourceTables.put(ResourceType.WOOL, new ResourceTable(ResourceType.WOOL, false));
		resourceTables.put(ResourceType.GRAIN, new ResourceTable(ResourceType.GRAIN, false));
		resourceTables.put(ResourceType.ORE, new ResourceTable(ResourceType.ORE, false));
		resourceTables.put(ResourceType.BRICK, new ResourceTable(ResourceType.BRICK, false));
		resourceTables.put(ResourceType.CLOTH, new ResourceTable(ResourceType.CLOTH, false));
		resourceTables.put(ResourceType.COIN, new ResourceTable(ResourceType.COIN, false));
		resourceTables.put(ResourceType.PAPER, new ResourceTable(ResourceType.PAPER, false));
		for(ResourceTable table:resourceTables.values()){
			this.add(table);
			this.row();
		}
		goldTable=new ResourceTable(ResourceType.PAPER, true);
		this.add(goldTable);
		this.row();
		this.add(labelRemaining=new Label("Choose 0 more resources/commodities to receive", Skins.METAL));
		this.row();
		buttonSendOffer=new TextButton("Send offer", Skins.METAL);
		buttonCancel=new TextButton("Cancel", Skins.METAL);
		buttonAccept=new TextButton("Accept", Skins.METAL);
		buttonRefuse=new TextButton("Refuse", Skins.METAL);
		group=new HorizontalGroup();
		this.add(group);
		layoutButtons();
	}
	
	public int getGold(){
		return goldTable.give;
	}
	
	private void calculateBestRate(){
		for(ResourceType type:ResourceType.values()){
			bestRate.put(type, 4);
			rateInformation.put(type, "bank");
		}
		if(map.getTradeMetropolis().getCity()!=null && map.getTradeMetropolis().getCity().getOwner()==player){
			for(ResourceType type:ResourceType.values()){
				if(Resource.isCommodity(type)){
					bestRate.put(type, 2);
					rateInformation.put(type, "Trading house");
				}
			}
		}
		if(player.getTurnInfo().getFleetResource()!=null){
			bestRate.put(player.getTurnInfo().getFleetResource(), 2);
			rateInformation.put(player.getTurnInfo().getFleetResource(), "merchant fleet");
		}
		
		if(map.getMerchant().getLocation()!=null && map.getMerchant().getOwner()==player){
			Tile tile=map.getMerchant().getLocation();
			ResourceType type=Resource.resourceFromTile(tile.getTileType());
			bestRate.put(type, 2);
			rateInformation.put(type, "merchant");
		}
		for(Harbor harbor:map.getHarbors().values()){
			Edge edge=harbor.getEdge();
			boolean valid=((edge.first.getConstruction() instanceof City || edge.first.getConstruction() instanceof Settlement) && edge.first.getConstruction().getOwner()==player);
			valid|=valid=((edge.second.getConstruction() instanceof City || edge.second.getConstruction() instanceof Settlement) && edge.second.getConstruction().getOwner()==player);
			if(valid){
				String harborType=harbor.isGeneralHarbor()?"generic harbor":"specific harbor";
				if(harbor.isGeneralHarbor()){
					for(ResourceType type:bestRate.keySet()){
						int current=bestRate.get(type);
						if(harbor.getRate()<current){
							bestRate.put(type, harbor.getRate());
							rateInformation.put(type, harborType);
						}
					}
				}else{
					ResourceType res=Resource.resourceFromTile(harbor.getType());
					ResourceType com=Resource.commodityFromTile(harbor.getType());
					int currentRate=bestRate.get(res);
					int currentCommodityRate=4;
					if(com!=null)
						currentCommodityRate=bestRate.get(com);
					if(harbor.getRate()<currentRate || (com!=null && harbor.getRate()<currentCommodityRate)){
						bestRate.put(res, harbor.getRate());
						rateInformation.put(res, harborType);
						if(com!=null){
							bestRate.put(com, harbor.getRate());
							rateInformation.put(com, harborType);
						}
					}
				}
			}
		}
	}
	
	private void layoutButtons(){
		group.clearChildren();
		if(!isAnswer){
			group.addActor(buttonSendOffer);
			group.addActor(buttonCancel);
		}else{
			group.addActor(buttonAccept);
			group.addActor(buttonRefuse);
		}
		this.pack();
	}
	
	public void setOffer(HashMap<ResourceType, Integer> wanted, HashMap<ResourceType, Integer> offered){
		for(ResourceType type:wanted.keySet()){
			ResourceTable resourceTable=resourceTables.get(type);
			resourceTable.setTake(offered.get(type));
			resourceTable.setGive(wanted.get(type));
		}
	}
	
	public void setGold(int gold){
		goldTable.take=gold;
	}
	
	public Button getButtonCancel(){
		return this.buttonCancel;
	}
	
	public Button getButtonAccept(){
		return this.buttonAccept;
	}
	
	public Button getButtonRefuse(){
		return this.buttonRefuse;
	}
	
	public void hide(){
		TradeWindow.this.setVisible(false);
		for(ResourceTable table:resourceTables.values()){
			table.give=0;
			table.take=0;
			totalTake=0;
			totalGive=0;
		}
		goldTable.give=0;
	}
	
	@Override
	public void act(float delta) {
		// TODO Auto-generated method stub
		super.act(delta);
		labelRemaining.setVisible(isBankTrade);
		labelRemaining.setText("Choose "+(totalGive-totalTake)+" more resources/commodities to receive");
		this.pack();
	}
	
	public void show(boolean isBankTrade, boolean isAnswer, String playerName){
		this.toFront();
		this.isBankTrade=isBankTrade;
		this.isAnswer=isAnswer;
		if(isBankTrade){
			calculateBestRate();
			this.getTitleLabel().setText("Trade with bank");
		}else
			this.getTitleLabel().setText("Trade with "+playerName);
		layoutButtons();
		this.setVisible(true);
	}
	
	public TextButton getSendButton(){
		return this.buttonSendOffer;
	}
	
	public HashMap<ResourceType, Integer> getWanted(){
		HashMap<ResourceType, Integer> wanted=new HashMap<ResourceType, Integer>();
		for(ResourceTable table:resourceTables.values())
			wanted.put(table.type, table.take);
		return wanted;
	}
	
	public HashMap<ResourceType, Integer> getOffered(){
		HashMap<ResourceType, Integer> offered=new HashMap<ResourceType, Integer>();
		for(ResourceTable table:resourceTables.values())
			offered.put(table.type, table.give);
		
		return offered;
	}
	
	public int getTotalGive(){
		return this.totalGive;
	}
	
	public int getTotalTake(){
		return this.totalTake;
	}
	
	class ResourceTable extends Table{
		
		ResourceType type;
		Label giveLabel;
		Label takeLabel;
		Label rateLabel;
		TextButton takeMore;
		TextButton takeLess;
		TextButton giveMore;
		TextButton giveLess;
		int give;
		int take;
		boolean gold;
		
		public ResourceTable(ResourceType type, boolean gold){
			this.type=type;
			this.gold=gold;
			this.add(rateLabel=new Label("", Skins.METAL)).width(200);
			this.add(giveLabel=new Label(" I give 0", Skins.METAL)).width(100);
			this.add(giveLess=new TextButton("-", Skins.METAL)).width(25);
			this.add(giveMore=new TextButton("+", Skins.METAL)).width(25);
			this.add(takeLabel=new Label(" I receive 0", Skins.METAL)).width(125);
			this.add(takeLess=new TextButton("-", Skins.METAL)).width(25);
			this.add(takeMore=new TextButton("+", Skins.METAL)).width(25);
			giveLabel.setAlignment(Align.right);
			takeLabel.setAlignment(Align.right);
			giveLess.addListener(createListener(-1, true));
			giveMore.addListener(createListener(1, true));
			takeLess.addListener(createListener(-1, false));
			takeMore.addListener(createListener(1, false));
		}
		
		private ClickListener createListener(final int change, final boolean giving){
			return new ClickListener(){
				@Override
				public void clicked(InputEvent event, float x, float y) {
					if(isAnswer)
						return;
					int actChange=change;
					if(isBankTrade && giving){
						if(!gold)
							actChange*=bestRate.get(type); // To change later with actual rate
						else actChange*=2;
					}
					if(giving && (give+actChange)>=0 && ((!gold && (give+actChange)<=resources.get(type)) || (gold && (give+actChange)<=player.getGold()))){
						if(totalTake==totalGive && actChange<0)
							return;
						give+=actChange;
						if(actChange>0)
							totalGive++;
						else totalGive--;
					}else if(!giving && (take+actChange>=0) && (take+actChange)<=20){
						if(!isBankTrade || (isBankTrade && (totalTake+actChange)<=totalGive)){
							take+=actChange;
							totalTake+=actChange;
						}
					}
				}
			};
		}
		
		public void setGive(int give){
			this.give=give;
		}
		
		public void setTake(int take){
			this.take=take;
		}
		
		@Override
		public void act(float delta) {
			super.act(delta);
			if(TradeWindow.this.isVisible()){
				if(!gold){
					if(isBankTrade)
						rateLabel.setText(type+" (Rate "+bestRate.get(type)+":1"+" from "+rateInformation.get(type)+")");
					else rateLabel.setText(type+"");
				}else{
					if(isBankTrade)
						rateLabel.setText("GOLD (Rate 2:1)");
					else rateLabel.setText("GOLD ");
					if(isAnswer){
						giveLabel.setVisible(false);
						giveLess.setVisible(false);
						giveMore.setVisible(false);
					}else{
						takeLabel.setVisible(false);
						takeLess.setVisible(false);
						takeMore.setVisible(false);
					}
				}
				giveLabel.setText(" I give "+give);
				takeLabel.setText(" I receive "+take);
			}
		}
		
	}
}
