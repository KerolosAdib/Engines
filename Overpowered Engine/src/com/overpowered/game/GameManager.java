package com.overpowered.game;

import java.awt.event.KeyEvent;

import com.overpowered.engine.AbstractGame;
import com.overpowered.engine.GameContainer;
import com.overpowered.engine.Renderer;
import com.overpowered.engine.audio.SoundClip;
import com.overpowered.engine.gfx.ImageTile;

public class GameManager extends AbstractGame
{
	private ImageTile image;
	private SoundClip clip;
	
	public GameManager()
	{
		image = new ImageTile("/Untitled.png", 16, 16);
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
		r.drawImageTile(image, gc.getInput().getMouseX() - 8, gc.getInput().getMouseY() - 16, (int)temp, 0);
		
	}

	public static void main(String[] args)
	{
		GameContainer gc = new GameContainer(new GameManager());
		gc.start();
	}
}
