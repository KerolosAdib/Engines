package com.overpowered.game;

import java.awt.event.KeyEvent;

import com.overpowered.engine.AbstractGame;
import com.overpowered.engine.GameContainer;
import com.overpowered.engine.Renderer;
import com.overpowered.engine.audio.SoundClip;
import com.overpowered.engine.gfx.Image;
import com.overpowered.engine.gfx.ImageTile;

public class GameManager extends AbstractGame
{
	private Image image;
	private ImageTile image2;
	private SoundClip clip;
	
	public GameManager()
	{
		image = new Image("/test.png");
		image2 = new ImageTile("/test2.png", 16, 16);
		image2.setAlpha(true);
		clip = new SoundClip("/audio/untitled.wav");
		clip.setVolume(-20f);
	}
	
	@Override
	public void update(GameContainer gc, float dt)
	{
		// TODO Auto-generated method stub
		if (gc.getInput().isKeyDown(KeyEvent.VK_A))
		{
			clip.play();
		}
		temp += dt * 20;
		if (temp > 4)
			temp = 0;
	}

	float temp = 0;
	
	@Override
	public void render(GameContainer gc, Renderer r)
	{
		for (int x = 0; x < image.getW(); x++)
		{
			for (int y = 0; y < image.getH(); y++)
			{
				r.setLightMap(x, y, image.getP()[x + y * image.getW()]);
			}
		}
		r.drawImage(image2, gc.getInput().getMouseX(), gc.getInput().getMouseY());
	}

	public static void main(String[] args)
	{
		GameContainer gc = new GameContainer(new GameManager());
		gc.start();
	}
}
