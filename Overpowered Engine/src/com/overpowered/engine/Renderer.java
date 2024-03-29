package com.overpowered.engine;

import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import com.overpowered.engine.gfx.Font;
import com.overpowered.engine.gfx.Image;
import com.overpowered.engine.gfx.ImageRequest;
import com.overpowered.engine.gfx.ImageTile;

public class Renderer
{
	
	
	private int pW, pH;
	private int[] p;
	private int[] zb;
	private int[] lm;
	private int[] lb;
	
	private int ambientColor = 0xff6b6b6b;
	private int zDepth = 0;
	private boolean processing = false;
	
	private Font font = Font.STANDARD;
	private ArrayList<ImageRequest> imageRequest = new ArrayList<ImageRequest>();
	
	public Renderer(GameContainer gc)
	{
		pW = gc.getWidth();
		pH = gc.getHeight();
		p = ((DataBufferInt) gc.getWindow().getImage().getRaster().getDataBuffer()).getData();
		zb = new int[p.length];
		lm = new int[p.length];
		lb = new int[p.length];
	}
	
	public void clear()
	{
		for (int i = 0; i < p.length; i++)
		{
			p[i] = 0;
			zb[i] = 0;
			lm[i] = ambientColor;
			lb[i] = 0;
		}
	}
	
	public void process()
	{
		processing = true;
		
		Collections.sort(imageRequest, new Comparator<ImageRequest>() {
			@Override
			public int compare(ImageRequest i0, ImageRequest i1)
			{
				if (i0.zDepth < i1.zDepth)
					return -1;
				if (i0.zDepth > i1.zDepth)
					return 1;
				return 0;
			}
		});
		
		for (int i = 0; i < imageRequest.size(); i++)
		{
			ImageRequest ir = imageRequest.get(i);
			setzDepth(ir.zDepth);
			drawImage(ir.image, ir.offX, ir.offY);
		}
		
		for (int i = 0; i < p.length; i++)
		{
			float r = ((lm[i] >> 16) & 0xff) / 255f;
			float g = ((lm[i] >> 8) & 0xff) / 255f;
			float b = ((lm[i]) & 0xff) / 255f;
			
			p[i] = ((int)(((p[i] >> 16) & 0xff) * r) << 16) | ((int)(((p[i] >> 8) & 0xff) * g) << 8) | ((int)(((p[i]) & 0xff) * b));
		}
		imageRequest.clear();
		processing = false;
	}
	
	public void setPixel(int x, int y, int value)
	{
		int alpha = ((value >> 24) & 0xff);
		
		if ((x < 0 || x >= pW || y < 0 || y >= pH) || alpha == 0)
		{
			return;
		}
		
		int index = x + y * pW;
		
		if (zb[index] > zDepth)
			return;
		zb[index] = zDepth;
		if (alpha == 255)
		{
			p[index] = value;
		}
		else
		{
			int color = 0;
			int pixelColor = p[index];
			int newRed = ((pixelColor >> 16) & 0xff) - (int)((((pixelColor >> 16) & 0xff) - ((value >> 16) & 0xff)) * alpha / 255f); 
			int newGreen = ((pixelColor >> 8) & 0xff) - (int)((((pixelColor >> 8) & 0xff) - ((value >> 8) & 0xff)) * alpha / 255f);
			int newBlue = (pixelColor & 0xff) - (int)(((pixelColor & 0xff) - (value & 0xff)) * (alpha / 255f));
			
			p[index] = (255 << 24 | newRed << 16 | newGreen << 8 | newBlue);
		}
	}
	
	public void setLightMap(int x, int y, int value)
	{
		if (x < 0 || x >= pW || y < 0 || y >= pH)
		{
			return;
		}
		
		int baseColor = lm[x + y * pW];
		
		int maxRed = Math.max(((baseColor >> 16) & 0xff), ((value >> 16) & 0xff));
		int maxGreen = Math.max(((baseColor >> 8) & 0xff), ((value >> 8) & 0xff));
		int maxBlue = Math.max(((baseColor) & 0xff), ((value) & 0xff));
		lm[x + y * pW] = (maxRed << 16 | maxGreen << 8 | maxBlue);
	}
	
	public void drawText(String text, int offX, int offY, int color)
	{
		int offset = 0;
		for (int i = 0; i < text.length(); i++)
		{
			int unicode = text.codePointAt(i);
			for (int y = 0; y < font.getFontImage().getH(); y++)
			{
				for (int x = 0; x < font.getWidths()[unicode]; x++)
				{
					if (font.getFontImage().getP()[(x + font.getOffsets()[unicode]) + y * font.getFontImage().getW()] == 0xffffffff)
					{
						setPixel(x + offX + offset, y + offY, color);
					}
				}
			}
			offset += font.getWidths()[unicode];
		}
	}
	
	public void drawImage(Image image, int offX, int offY)
	{
		if (image.isAlpha() && !processing)
		{
			imageRequest.add(new ImageRequest(image, zDepth, offX, offY));
			return;
		}
		if (offX < -image.getW())
			return;
		if (offY < -image.getH())
			return;
		if (offX >= pW)
			return;
		if (offY >= pH)
			return;
		
		int newX = 0;
		int newY = 0;
		int newWidth = image.getW();
		int newHeight = image.getH();
		

		if (offX < 0)
			newX -= offX;
		if (offY < 0)
			newY -= offY;
		if (newWidth + offX > pW)
		{
			newWidth -= newWidth + offX - pW;
		}
		
		if (newHeight + offY > pH)
		{
			newHeight -= newHeight + offY - pH;
		}
		
		for (int y = 0; y < newHeight; y++)
		{
			for (int x = 0; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, image.getP()[x + y * image.getW()]);
			}
		}
	}
	
	public void drawImageTile(ImageTile image, int offX, int offY, int tileX, int tileY)
	{
		if (image.isAlpha() && !processing)
		{
			imageRequest.add(new ImageRequest(image.getTileImage(tileX, tileY), zDepth, offX, offY));
			return;
		}
		
		if (offX < -image.getTileW())
			return;
		if (offY < -image.getTileH())
			return;
		if (offX >= pW)
			return;
		if (offY >= pH)
			return;
		
		int newX = 0;
		int newY = 0;
		int newWidth = image.getTileW();
		int newHeight = image.getTileH();
		

		if (offX < 0)
			newX -= offX;
		if (offY < 0)
			newY -= offY;
		if (newWidth + offX > pW)
		{
			newWidth -= newWidth + offX - pW;
		}
		
		if (newHeight + offY > pH)
		{
			newHeight -= newHeight + offY - pH;
		}
		
		for (int y = 0; y < newHeight; y++)
		{
			for (int x = 0; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, image.getP()[(x + tileX * image.getTileW()) + (y + tileY * image.getTileH()) * image.getW()]);
			}
		}
	}
	
	public void drawRect(int offX, int offY, int width, int height, int color) 
	{
		for (int y = 0; y <= height; y++)
		{
			setPixel(offX, y + offY, color);
			setPixel(offX + width, y + offY, color);
		}
		
		for (int x = 0; x <= width; x++)
		{
			setPixel(x + offX, offY, color);
			setPixel(x + offX, offY + height, color);
		}
	}
	
	public void drawFillRect(int offX, int offY, int width, int height, int color)
	{
		if (offX < -width)
			return;
		if (offY < -height)
			return;
		if (offX >= pW)
			return;
		if (offY >= pH)
			return;
		
		int newX = 0;
		int newY = 0;
		int newWidth = width;
		int newHeight = height;
		

		if (offX < 0)
			newX -= offX;
		if (offY < 0)
			newY -= offY;
		if (newWidth + offX > pW)
		{
			newWidth -= newWidth + offX - pW;
		}
		
		if (newHeight + offY > pH)
		{
			newHeight -= newHeight + offY - pH;
		}
		for (int y = newY; y < newHeight; y++)
		{
			for (int x = newX; x < newWidth; x++)
			{
				setPixel(x + offX, y + offY, color);
			}
		}
	}

	public int getzDepth()
	{
		return zDepth;
	}

	public void setzDepth(int zDepth)
	{
		this.zDepth = zDepth;
	}
}
