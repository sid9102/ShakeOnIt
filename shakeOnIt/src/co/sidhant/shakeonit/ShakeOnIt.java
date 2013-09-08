package co.sidhant.shakeonit;

import java.io.IOException;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.TextInputListener;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class ShakeOnIt implements ApplicationListener {
	private OrthographicCamera camera;
	private SpriteBatch menuBatch;
	private float selectorPadding;
	private float[] title;
	private float[] selectors;
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
	private boolean drawMenu;
	private Preferences shakePrefs;
	private nameListener nl;
	
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
			Gdx.app.log("name", name);
		}
		
		// Init the coords for title and selector buttons, and padding
		selectorPadding = h / 12f;
		title = new float[4];
		selectors = new float[4];
		title[0] = w / 6f;
		title[1] = h - (h/9f + h/4f);
		title[2] = (w / 3f) * 2f;
		title[3] = h / 4f;
		
		selectors[0] = 0;
		selectors[1] = h * 7f / 18f;
		selectors[2] = w * 2/ 3f;
		selectors[3] = h * 5/ 36f;
		// Counter to handle "breathing" of the title image
		breathCount = 0;
		// breathing direction
		breathDir = 0.5f;
		drawMenu = true;
		nl = new nameListener();
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
	public void render() {		
		Gdx.gl.glClearColor(0.2f, 0.8f, 1, 1); //blue, e5bc10
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		camera.update();
		if(breathCount == 10)
		{
			breathDir = -breathDir;
			breathCount = 0;
		}
		
		for(int i = 0; i < 4; i++)
		{
			title[i] += breathDir;
		}
		breathCount++;
		if(drawMenu)
		{
			menuBatch.setProjectionMatrix(camera.combined);
			menuBatch.begin();
			menuBatch.draw(titleReg, title[0], title[1], title[2], title[3]);
			menuBatch.draw(selecReg, selectors[0], selectors[1], selectors[2], selectors[3]);
			menuBatch.draw(selecReg, selectors[0], selectors[1] - (selectorPadding + selectors[3]), selectors[2], selectors[3]);
			menuBatch.draw(startTex, selectors[0], selectors[1], selectors[2], selectors[3]);
			menuBatch.draw(settingsTex, selectors[0], selectors[1] - (selectorPadding + selectors[3]), selectors[2], selectors[3]);
			float textPos = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
			// Set the scale based on screen res and length of name
			shakeFont.setScale(selectorPadding / (6f * name.length()));
			shakeFont.draw(menuBatch, "Hi, " + name + ".", 0, textPos);
			menuBatch.end();
		}
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
	public void resume() {
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
		   public boolean keyUp (int keycode) {
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
		   public boolean touchUp (int x, int y, int pointer, int button) {
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
