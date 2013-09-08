package co.sidhant.shakeonit;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ShakeOnIt implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch menuBatch;
	private float selectorPadding;
	private float[] title;
	private float[] selectors;
	private float[] titleTarget;
	private float[] selectorsTarget;
	private Texture titleTex;
	private Texture selecTex;
	private Texture startTex;
	private Texture settingsTex;
	private TextureRegion titleReg;
	private TextureRegion selecReg;
	private int breathCount;
	private float breathDir;
	private boolean nameExists;
	private String name;
	private BitmapFont shakeFont;
	private float textPosX;
	private float textPosXMax;
	private boolean drawMenu;
	private boolean drawSettings;
	private Preferences shakePrefs;
	private nameListener nl;
	private boolean animate;

	@Override
	public void create() {		
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();

		camera = new OrthographicCamera(w, h);
		camera.translate(w/2, h/2);

		menuBatch = new SpriteBatch();
		titleTex = new Texture(Gdx.files.internal("title.png"));
		selecTex = new Texture(Gdx.files.internal("selector.png"));
		startTex = new Texture(Gdx.files.internal("start.png"));
		settingsTex = new Texture(Gdx.files.internal("settings.png"));
		titleReg = new TextureRegion(titleTex, 512, 256);
		selecReg = new TextureRegion(selecTex, 512, 256);
		shakeFont = new BitmapFont(Gdx.files.internal("shakeFont.fnt"), Gdx.files.internal("shakeFont0.png"), false);

		//If this is the first run, get the name of the user
		shakePrefs = Gdx.app.getPreferences("prefs");
		nameExists = shakePrefs.contains("name");
		name = "???";
		if(nameExists)
		{
			name = shakePrefs.getString("name");
			//Gdx.app.log("name", name);
		}

		// Init the coords for title and selector buttons, and padding
		selectorPadding = h / 12f;
		title = new float[4];
		selectors = new float[6];

		title[0] = w / 6f;			//x
		title[1] = h; 				//y
		title[2] = (w / 3f) * 2f;	//width
		title[3] = h / 4f;			//height

		selectors[1] = h * 7f / 18f;
		selectors[2] = w * 2/ 3f;
		selectors[0] = 0 - selectors[2];	//Start with the selectors off screen
		selectors[3] = h * 5/ 36f;
		selectors[4] = selectors[0] - (selectors[2] / 2); // settings button x
		selectors[5] = selectors[1] - (selectorPadding + selectors[3]); // settings button y

		//Targets for final positions of menu objects
		titleTarget = new float[4];
		selectorsTarget = new float[6];

		titleTarget[0] = w / 6f;
		titleTarget[1] = h - (h/9f + h/4f);
		titleTarget[2] = (w / 3f) * 2f;
		titleTarget[3] = h / 4f;

		selectorsTarget[0] = 0;
		selectorsTarget[1] = h * 7f / 18f;
		selectorsTarget[2] = w * 2/ 3f;
		selectorsTarget[3] = h * 5/ 36f;
		selectorsTarget[4] = selectorsTarget[0]; //settings x
		selectorsTarget[5] = selectorsTarget[1] - (selectorPadding + selectors[3]); // settings button y
		// Counter to handle "breathing" of the title image
		breathCount = 0;
		// breathing direction
		breathDir = 0.5f;
		drawMenu = true;
		nl = new nameListener();
		MyInputProcessor input = new MyInputProcessor();
		Gdx.input.setInputProcessor(input);
		Gdx.input.setCatchBackKey(true);
		textPosX = 0;
		textPosXMax = w;
		// Boolean that determines whether to animate, when changing screens
		animate = true;
		drawSettings = false;
	}

	@Override
	public void dispose() {
		menuBatch.dispose();
		selecTex.dispose();
		titleTex.dispose();
		startTex.dispose();
		settingsTex.dispose();
		shakeFont.dispose();
	}

	@Override
	public void render() 
	{		
		Gdx.gl.glClearColor(0.2f, 0.8f, 1, 1); //blue, e5bc10
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();

		//Draw the menu
		if(drawMenu)
		{
			name = shakePrefs.getString("name");
			menuBatch.setProjectionMatrix(camera.combined);
			menuBatch.begin();
			// This means the menu has just been accessed, animate the menu 
			if(animate)
			{
				menuBatch.draw(titleReg, title[0], title[1], title[2], title[3]);
				menuBatch.draw(selecReg, selectors[0], selectors[1], selectors[2], selectors[3]);
				menuBatch.draw(startTex, selectors[0], selectors[1], selectors[2], selectors[3]);
				menuBatch.draw(selecReg, selectors[4], selectors[5], selectors[2], selectors[3]);
				menuBatch.draw(settingsTex, selectors[4], selectors[5], selectors[2], selectors[3]);
				float textPosY = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
				// Set the scale based on screen res
				shakeFont.setScale(selectorPadding / 18f);
				// Text scrolling
				float textPosXLoop =  0 - shakeFont.getBounds("Hi, " + name + ".").width;
				if(textPosX < textPosXLoop)
				{
					textPosX = textPosXMax;
				}
				// Move the text by a fraction of the title width per frame
				textPosX -= titleTarget[2] / 96f;
				shakeFont.draw(menuBatch, "Hi, " + name + ".", textPosX, textPosY);
				// move the elements into place
				boolean animDone = true;
				if(title[1] > titleTarget[1])
				{
					title[1] -= title[3] / 25;
					animDone = false;
				}
				else
				{
					title[1] = titleTarget[1];
				}

				if(selectors[0] < selectorsTarget[0])
				{
					selectors[0] += selectors[2] / 20;
					animDone = false;
				}
				else
				{
					selectors[0] = selectorsTarget[0];
				}
				
				if(selectors[4] < selectorsTarget[4])
				{
					selectors[4] += selectors[2] / 20;
					animDone = false;
				}
				else
				{
					selectors[4] = selectorsTarget[4];
				}
				
				if(animDone)
				{
//					Gdx.app.log("animation", "done");
					animate = false;
					resetCoords();
				}
			}
			else
			{
				if(breathCount == 15)
				{
					breathDir = -breathDir;
					breathCount = 0;
				}

				for(int i = 0; i < 4; i++)
				{
					titleTarget[i] += breathDir;
				}
				breathCount++;
				menuBatch.draw(titleReg, titleTarget[0], titleTarget[1], titleTarget[2], titleTarget[3]);
				menuBatch.draw(selecReg, selectorsTarget[0], selectorsTarget[1], selectorsTarget[2], selectorsTarget[3]);
				menuBatch.draw(selecReg, selectorsTarget[4], selectorsTarget[5], selectorsTarget[2], selectorsTarget[3]);
				menuBatch.draw(startTex, selectorsTarget[0], selectorsTarget[1], selectorsTarget[2], selectorsTarget[3]);
				menuBatch.draw(settingsTex, selectorsTarget[4], selectorsTarget[5], selectorsTarget[2], selectorsTarget[3]);
				float textPosY = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
				// Set the scale based on screen res
				shakeFont.setScale(selectorPadding / 18f);
				// Make the greeting text scroll from right to left.
				float textPosXLoop =  0 - shakeFont.getBounds("Hi, " + name + ".").width;
				if(textPosX < textPosXLoop)
				{
					textPosX = textPosXMax;
				}
				// Move the text by a fraction of the title width per frame
				textPosX -= titleTarget[2] / 96f;
				shakeFont.draw(menuBatch, "Hi, " + name + ".", textPosX, textPosY);
			}
			menuBatch.end();
		}
		// Check for first run
		if(!nameExists)
		{
			Gdx.input.getTextInput(nl, "Your name?", "???");
			if(nl.nameText != null)
			{
				name = nl.nameText;
			}
			else
			{
				name = "???";
			}
			nameExists = true;
			shakePrefs.putString("name", name);
			shakePrefs.flush();
		}

		if(name == "???" && nl.nameText != null)
		{
			name = nl.nameText;
			shakePrefs.putString("name", name);
			shakePrefs.flush();
		}
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() 
	{
		resetCoords();
		animate = true;
	}

	//Reset start positions of menu elements
	private void resetCoords()
	{
		float w = Gdx.graphics.getWidth();
		float h = Gdx.graphics.getHeight();
		title[0] = w / 6f;			//x
		title[1] = h; 				//y
		title[2] = (w / 3f) * 2f;	//width
		title[3] = h / 4f;			//height

		selectors[1] = h * 7f / 18f;
		selectors[2] = w * 2/ 3f;
		selectors[0] = 0 - selectors[2];	//Start with the selectors off screen
		selectors[3] = h * 5/ 36f;
		selectors[4] = selectors[0] - (selectors[2] / 2); // settings button x
		selectors[5] = selectors[1] - (selectorPadding + selectors[3]); // settings button y
	}
	
	public class nameListener  implements TextInputListener{
		public String nameText;
		@Override
		public void input(String text) 
		{
			nameText = text;
			//			Gdx.app.log("name", text);
		}

		@Override
		public void canceled() 
		{
			nameText = "???";
		}

	}

	public class MyInputProcessor implements InputProcessor {
		@Override
		public boolean keyDown (int keycode) {
			return false;
		}

		@Override
		public boolean keyUp (int keycode) 
		{
			if(keycode == 4)
			{
				if(drawSettings)
				{
					animate = true;
					drawSettings = false;
					drawMenu = true;
				}
				
				if(drawMenu)
				{
					
				}
			}
			return false;
		}

		@Override
		public boolean keyTyped (char character) {
			return false;
		}

		@Override
		public boolean touchDown (int x, int y, int pointer, int button) {
			return false;
		}

		@Override
		public boolean touchUp (int x, int y, int pointer, int button) 
		{
			Gdx.app.log("touchUp", Integer.toString(x) + ", " + Integer.toString(y));
			return false;
		}

		@Override
		public boolean touchDragged (int x, int y, int pointer) {
			return false;
		}

		@Override
		public boolean scrolled (int amount) {
			return false;
		}

		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			// TODO Auto-generated method stub
			return false;
		}
	}
}
