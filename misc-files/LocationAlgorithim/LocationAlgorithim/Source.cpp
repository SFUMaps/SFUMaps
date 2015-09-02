/*An algorithim for wireless interpolation
 By: Bosco Noronha*/

#include <iostream>
#include <Windows.h>

int main() {

	int mapCounter = 0;
	int map[10][10] = {
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
		0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
	}

	for (int i = 0; i < 10; i++) {
		for (int j = 0; j < 10; j++) {
			if (mapCounter == 10) {
				std::cout << "" << std::endl;
				mapCounter = 0;
			}
			if (map[i][j] == 1) {
				std::cout << "A";
			}
			else if (map[i][j] == 0) {
				std::cout << " ";
			}
			mapCounter++;
		}
	}

	system("pause>nul");
	return 0;
}
