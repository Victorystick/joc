// should output: 14

class TspMC {
	public static void main(String[] args) {
		Tsp tsp;
		int[] x;
		int[] y;

		tsp = new Tsp();
		x = new int[6];
		y = new int[6];

		x[0] = 0;
		y[0] = 0;

		x[1] = 10;
		y[1] = 0-2;

		x[2] = 8;
		y[2] = 2;

		x[3] = 10;
		y[3] = 0;

		x[4] = 10;
		y[4] = 2;

		x[5] = 6;
		y[5] = 2;

		System.out.println(tsp.tspDis(x, y));
	}
}

class Tsp {
	public int tspDis(int[] x, int[] y)
	{
		int i;
		int j;
		int a;
		int[] nx;
		int[] ny;
		int best;
		int tmp;

		best = 32768;

		if(x.length == 2)
		{
			best = this.sqrt((x[1]-x[0])*(x[1]-x[0]) + (y[1]-y[0])*(y[1]-y[0]));
		}
		else
		{
			nx = new int[x.length-1];
			ny = new int[x.length-1];

			i = 1;
			while(i < x.length)
			{
				j = 1;
				a = 1;
				while(j < x.length)
				{
					if(i == j)
					{
						nx[0] = x[j];
						ny[0] = y[j];
					}
					else
					{
						nx[a] = x[j];
						ny[a] = y[j];
						a = a + 1;
					}
					j = j + 1;
				}

				tmp = this.tspDis(nx,ny);
				tmp = tmp + this.sqrt((x[0]-nx[0])*(x[0]-nx[0]) + (y[0]-ny[0])*(y[0]-ny[0]));

				if(tmp < best)
				{
					best = tmp;
				}
				else {}

				i = i + 1;
			}
		}
		return best;
	}

	public int sqrt(int x)
	{
		int ret;
		if(x > 32768)
		{
			ret = 0-1;
		}
		else
		{
			ret = this.sqrtS(0, 32768, x);
		}
		return ret;
	}

	public int sqrtS(int l, int r, int x)
	{
		int m;
		int ret;

		m = this.div(r-l, 2) + l;

		if(l >= r)
		{
			ret = m;
		} else {
			if(x < m*m)
			{
				ret = this.sqrtS(l, m-1, x);
			}
			else
			{
				ret = this.sqrtS(m+1, r, x);
			}
		}
		return ret;
	}

	public int div(int x, int y)
	{
		int out;
		out = 0;

		while(!(x < y))
		{
			x = x - y;
			out = out + 1;
		}

		return out;
	}
}
