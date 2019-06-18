#include <iostream>
#include <string>
#include <bitset>
#include <vector>
#include <fstream>
#include <exception>
#include <experimental/filesystem>
#include <opencv2/opencv.hpp>
#include <filesystem>
using namespace std;
using namespace cv;
namespace fs = std::experimental::filesystem;

const string _delimiter = "11111110";
void encode();
void decode();
void convertMessageToBinary(vector<string>&, map<string, char>&, string);
string generateEncodedImage(Mat&, string);
void embedMessage(Mat&, string, Mat&);
string retrieveEncodedMessageFromImage(string);
bool checkIfFileExists(string);

class WrongMenuChoiceException : public exception
{
public:
	virtual const char* what() const throw()
	{
		return "Invalid Choice! Please enter a number from 1 to 3!";
	}
} wrongChoiceEx;

int main()
{
	//Mat img = imread("original.jpg");
	
	/*cout << "B - " << bitset<8>(img.at<cv::Vec3b>(0, 0)[0]) << " G - " << bitset<8>(img.at<cv::Vec3b>(0, 0)[1]) << " R - " << bitset<8>(img.at<cv::Vec3b>(0, 0)[2]);
	namedWindow("image", WINDOW_NORMAL);
	imshow("image", img);
	waitKey(0);*/

	/*Mat newImg;
	embedMessage(img, "I like computer class", newImg);
	cout << generateEncodedImage(newImg, "new1.jpg");

	namedWindow("Original_Image", WINDOW_NORMAL);
	imshow("Original_Image", img);

	namedWindow("Encoded_Image", WINDOW_NORMAL);
	imshow("Encoded_Image", newImg);
	waitKey(0);*/

	do
	{
		try
		{
			string userChoice;

			cout << "Hello! Welcome to Image Encoder (Data in Picture)!" << endl;
			cout << "Created by: Jose A. Alvarado" << endl;
			cout << "Copyright J.A.A. Productions 2019" << endl;
			cout << endl;
			cout << "Please select an option..." << endl;
			cout << "1. Encode" << endl;
			cout << "2. Decode" << endl;
			cout << "3. Quit" << endl;
			cout << endl;
			cout << "Choice: ";
			getline(cin, userChoice);

			switch (stoi(userChoice, nullptr, 10))
			{
			case 1:
				cout << endl;
				encode();
				break;
			case 2:
				cout << endl;
				decode();
				break;
			case 3:
				cout << "Goodbye!" << endl;
				cout << endl;
				system("pause");
				return 0;
			default:
				throw wrongChoiceEx;
				break;
			}
		}
		catch (exception &e)
		{
			cout << e.what() << endl << endl;
			cv::destroyAllWindows();
		}
	} while (true);
}

void encode()
{
	string nameInput;
	string originalPath;
	string newPath;
	string encodedImgSaveLoc;

	cout << "Enter your message: ";
	getline(cin, nameInput);
	cout << endl;
	cout << "Enter the location of the image: ";
	getline(cin, originalPath);
	cout << endl;

	if (!checkIfFileExists(originalPath))
		throw exception("Picture does not exist!");

	cout << "Enter the location of the encoded image: ";
	getline(cin, newPath);
	cout << endl;

	Mat img = imread(originalPath);
	Mat newImg;
	embedMessage(img, nameInput, newImg);
	encodedImgSaveLoc = generateEncodedImage(newImg, newPath);
	cout << endl;
	cout << "Encoded Image Saved At: " << encodedImgSaveLoc;
	cout << "Image encoded successfully!";

	namedWindow("Original_Image", WINDOW_NORMAL);
	imshow("Original_Image", img);

	namedWindow("Encoded_Image", WINDOW_NORMAL);
	imshow("Encoded_Image", newImg);
	waitKey(0);
	
	cout << endl << endl;
}

void decode()
{
	string picPath;

	cout << "Enter the location of the encoded image: ";
	getline(cin, picPath);

	if (!checkIfFileExists(picPath))
		throw exception("Picture does not exist!");

	cout << endl;
	cout << "The message is \"" << retrieveEncodedMessageFromImage(picPath) << "\"" << endl << endl;
}

string generateEncodedImage(Mat& image, string picturePath)
{
	char full[_MAX_PATH];
	_fullpath(full, picturePath.c_str(), _MAX_PATH);
	string pngPath = picturePath + ".png";

	/*vector<int> compression_params;
	compression_params.push_back(IMWRITE_PNG_COMPRESSION);
	compression_params.push_back(0);*/

	imwrite(pngPath, image);
	rename(pngPath.c_str(), picturePath.c_str());

	return full;
}

bool checkIfFileExists(string filename)
{
	char full[_MAX_PATH];
	_fullpath(full, filename.c_str(), _MAX_PATH);

	fs::path filePath = fs::path(full);

	return fs::exists(filePath);
}

void convertMessageToBinary(vector<string> &binaryNameList, map<string, char> &charBinMap, string name)
{
	cout << "Name: " << name << endl;
	cout << "Binary Representation: " << endl;

	for (int charIndex = 0; charIndex < name.length(); charIndex++)
	{
		string currCharBinRepresentation = bitset<8>(name[charIndex]).to_string();
		cout << currCharBinRepresentation << " ";
		charBinMap.insert(pair<string, char>(currCharBinRepresentation, name[charIndex]));
		binaryNameList.push_back(currCharBinRepresentation);
	}

	cout << endl;

	return;
}

void embedMessage(Mat &originalImg, string name, Mat &newImg)
{
	int picHeight = originalImg.rows;
	int picWidth = originalImg.cols;

	newImg = Mat(picHeight, picWidth, CV_8UC3, Scalar(0, 0, 255));

	vector<string> binaryConvertedNameList = vector<string>();
	map<string, char> binConvertedNameCharBinMap = map<string, char>();
	convertMessageToBinary(binaryConvertedNameList, binConvertedNameCharBinMap, name);

	ofstream outputStream = ofstream("debug.txt");

	outputStream << "Message: " << name << endl;
	outputStream << "Binary Representation: " << endl;

	string binConvertedNameString = "";
	for (int index = 0; index < binaryConvertedNameList.size(); index++)
	{
		binConvertedNameString.append(binaryConvertedNameList[index]);
		outputStream << binaryConvertedNameList[index] << " ";
	}
	binConvertedNameString.append(_delimiter);

	outputStream << endl << endl;
	outputStream << "Image in Binary..." << endl;

	int count = 0;
	for (int xIndex = 0; xIndex < picHeight; xIndex++)
	{
		for (int yIndex = 0; yIndex < picWidth; yIndex++)
		{
			Vec3b pixel = originalImg.at<Vec3b>(xIndex, yIndex);
			string pixelBlue = bitset<8>(pixel[0]).to_string();
			string pixelGreen = bitset<8>(pixel[1]).to_string();
			string pixelRed = bitset<8>(pixel[2]).to_string();
			string rgbBinStr = pixelRed + pixelGreen + pixelBlue;
			outputStream << "Pixel (x:" << xIndex << ", y:" << yIndex << "): " << endl;

			if (count < binConvertedNameString.length())
			{
				int currLetterIndex = count / 8;
				int currBitIndex = count % 8;

				if (currLetterIndex < binaryConvertedNameList.size())
				{
					string currLetterBin = binaryConvertedNameList[currLetterIndex];
					char *currLetter = new char[2];
					currLetter[0] = binConvertedNameCharBinMap[currLetterBin];
					currLetter[1] = NULL;

					string tempCurrLetterBinStr = currLetterBin;
					char *replaceString = new char[3];
					replaceString[0] = tempCurrLetterBinStr[currBitIndex];
					replaceString[1] = ']';
					replaceString[2] = NULL;
					tempCurrLetterBinStr.replace(currBitIndex, 1, "[");
					tempCurrLetterBinStr.insert(currBitIndex + 1, replaceString);

					outputStream << "Current Letter: " << (isspace(currLetter[0]) ? "[SPACE]" : currLetter) << " - " << tempCurrLetterBinStr << endl;
				}
				else
				{
					string tempDelimiterStr = _delimiter;
					char *replaceString = new char[3];
					replaceString[0] = tempDelimiterStr[currBitIndex];
					replaceString[1] = ']';
					replaceString[2] = NULL;
					tempDelimiterStr.replace(currBitIndex, 1, "[");
					tempDelimiterStr.insert(currBitIndex + 1, replaceString);

					outputStream << "Current Letter [DELIMITER] - " << tempDelimiterStr << endl;
				}

				string newRgbBinStr = rgbBinStr;
				newRgbBinStr[rgbBinStr.length() - 1] = binConvertedNameString[count];
				string newPixelRed = newRgbBinStr.substr(0, 8);
				string newPixelGreen = newRgbBinStr.substr(8, 8);
				string newPixelBlue = newRgbBinStr.substr(16, 8);

				Vec3b newPixel = Vec3b(stoi(newPixelBlue, nullptr, 2), stoi(newPixelGreen, nullptr, 2), stoi(newPixelRed, nullptr, 2));

				outputStream << rgbBinStr << " -> " << newRgbBinStr << endl;

				newImg.at<Vec3b>(xIndex, yIndex) = newPixel;
				count++;
			}
			else
			{
				newImg.at<Vec3b>(xIndex, yIndex) = pixel;
				outputStream << "NO ENCODING" << endl << rgbBinStr << endl;
			}

			outputStream << endl;
		}
	}

	outputStream.close();
	return;

}

string retrieveEncodedMessageFromImage(string picturePath)
{
	Mat encodedImg = imread(picturePath);
	int picHeight = encodedImg.rows;
	int picWidth = encodedImg.cols;

	String tempChar = "";
	String encodedMessageString = "";

	int count = 0;
	for (int xIndex = 0; xIndex < picHeight; xIndex++)
	{
		for (int yIndex = 0; yIndex < picWidth; yIndex++)
		{
			Vec3b pixel = encodedImg.at<Vec3b>(xIndex, yIndex);
			string pixelBlue = bitset<8>(pixel[0]).to_string();
			string pixelGreen = bitset<8>(pixel[1]).to_string();
			string pixelRed = bitset<8>(pixel[2]).to_string();
			string rgbBinStr = pixelRed + pixelGreen + pixelBlue;

			if (count < 8)
			{
				tempChar.append(rgbBinStr.substr(rgbBinStr.length() - 1, 1));
				count++;

				if (count == 8)
				{
					count = 0;

					if (tempChar.compare(_delimiter) == 0)
					{
						tempChar = "";
						goto outer;
					}
					else
					{

						encodedMessageString.append(string(1, stoi(tempChar, nullptr, 2)));
						tempChar = "";
					}
				}
			}
		}
	}

	outer:

	return encodedMessageString;
}