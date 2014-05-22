class Main {
	public static void main(String[] args) {
		int[] arr;
		int i;
		Sorter s;

		arr = new int[10];
		arr[0] = 5;
		arr[1] = 3;
		arr[2] = 3;
		arr[3] = 1;
		arr[4] = 2;
		arr[5] = 0;
		arr[6] = 9;
		arr[7] = 42;
		arr[8] = 17;
		arr[9] = 7;

		System.out.println(false);
		i = 0;
		while (i < arr.length) {
			System.out.println(arr[i]);
			i = i + 1;
		}

		s = new Sorter();
		arr = s.sort(arr);

		System.out.println(false);
		i = 0;
		while (i < arr.length) {
			System.out.println(arr[i]);
			i = i + 1;
		}
	}
}

class Sorter {
	public int[] sort(int[] arr) {
		int i;
		int j;
		int temp;

		i = 0;
		j = 1;
		while (i < arr.length) {
			while (j < arr.length) {
				if (arr[j] < arr[i]) {
					//Swap
					temp = arr[j];
					arr[j] = arr[i];
					arr[i] = temp;
				} else {

				}
				j = j+1;
			}
			i = i+1;
			j = i+1;
		}
		return arr;
	}
}
