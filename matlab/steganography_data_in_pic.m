% Hello! Welcome to Image Encoder/Decoder (Data in Picture)!
% Created By: Jose A. Alvarado
% 
% Copyright J.A.A. Productions 2019
% 
% % Initializations

clear
clc
global DELIMITER;
DELIMITER = '11111110';

while 1
    userChoice = input('Please select an option...\n1. Encode\n2. Decode\n3. Quit\n\nChoice: ');
    switch(userChoice)
        case 1
            fprintf('\n');
            encode();
        case 2
            fprintf('\n');
            decode();
        case 3
            disp('Goodbye!');
            break;
    end
end

fclose('all');
clear