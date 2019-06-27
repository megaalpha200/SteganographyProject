% Embed the Message into the Provided Image
% The function takes a 3x3 array which represents the image to be encoded and 
% a string message that will be encoded into the image. It returns a 3x3 array 
% which represents the new encoded image.

function newImg = embedMessage(originalImg, msg)
    [picHeight, picWidth, ~] = size(originalImg); %Get the img height and width
    newImg = originalImg; %Copy the original image to a new instance
    [~, msgSize] = size(msg); %Get length of msg
    
    fileID = fopen('debug.txt', 'wt');
    
    %Convert msg into binary and append the delimiter
    binConvertedMsgList = vertcat(convertMessageToBinary(msg, fileID), getDelimiter());
    
    fprintf(fileID, '\nImage in Binary...\n');
    
    innerLoopBroken = false; %Flag used to break out of both for loops
    count = 0; %Counter for each bit in the message
    for yIndex = 1:picHeight
        for xIndex = 1:picWidth
            %Get the corresponding RGB values for the current pixel
            originalPixel = num2str([dec2bin(originalImg(yIndex, xIndex, 1)) dec2bin(originalImg(yIndex, xIndex, 2)) dec2bin(originalImg(yIndex, xIndex, 3))]);
            
            newPixel = originalPixel;
            newPixelRed = dec2bin(originalImg(yIndex, xIndex, 1));
            newPixelGreen = dec2bin(originalImg(yIndex, xIndex, 2));
            newPixelBlue = dec2bin(originalImg(yIndex, xIndex, 3));
            
            fprintf(fileID, 'Pixel (x:%d, y:%d): \n', [xIndex, yIndex]);
            
            %Get current mesage bit and assign it to the least significant
            %bit of the blue pixel for newImg
            currRow = floor(count/8)+1; %Represents current letter msg
            currCol = floor(mod(count, 8)+1); %Represents current bit in the current letter
            currCharBin = binConvertedMsgList(currRow, :); %Represents the current letter in binary
            currMsgBit = binConvertedMsgList(currRow, currCol);
            newPixelBlue(1,end) = currMsgBit;
            newPixel(1,end) = currMsgBit;
            
            %Replace the RGB values of the current pixel
            newImg(yIndex, xIndex, 1) = bin2dec(newPixelRed);
            newImg(yIndex, xIndex, 2) = bin2dec(newPixelGreen);
            newImg(yIndex, xIndex, 3) = bin2dec(newPixelBlue);
            
            currCharBinWithSelectedBit = currCharBin;
            currCharBinWithSelectedBit = [currCharBinWithSelectedBit(1:currCol-1) '[' currCharBinWithSelectedBit(currCol) ']' currCharBinWithSelectedBit(currCol+1:end)];
            if count <= (msgSize*8)-1
                if (isspace(msg(currRow))); msgDispChar = '[SPACE]'; else; msgDispChar =  msg(currRow); end
                fprintf(fileID, 'Current Letter: %s - %s\n', msgDispChar, currCharBinWithSelectedBit);
            else
                fprintf(fileID, 'Current Letter: [DELIMITER] - %s\n', currCharBinWithSelectedBit);
            end
            fprintf(fileID, [originalPixel ' -> ' newPixel '\n\n']);
            
            %Check if every bit in the message has been encoded
            if count == ((msgSize+1)*8)-1
                innerLoopBroken = true;
                break;
            end
            
            count = count + 1;
        end
        
        if innerLoopBroken == true
            break;
        end
    end
    fclose(fileID);
end