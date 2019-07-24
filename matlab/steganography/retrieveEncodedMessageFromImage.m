% Retrieve the Message from the Picture
% The function takes a 3x3 array which represents the encoded image. It returns 
% a string which is the encoded message

function encodedMessage = retrieveEncodedMessageFromImage(encodedImg)
    startDateTime = datetime('now', 'Format', 'dd-MM-yyyy HH:mm:ss.SSS');

    [picHeight, picWidth, ~] = size(encodedImg); %Get the img height and width
    
    tempCharBin = '';
    encodedMessage = '';
    
    innerLoopBroken = false; %Flag used to break out of both for loops
    count = 0; %Counter to keep track of each 8-bit character read
    charCount = 1; %Counter to keep track the number of retrieved characters
    for yIndex = 1:picHeight
        for xIndex = 1:picWidth
            imgBluePixel = dec2bin(encodedImg(yIndex, xIndex, 3));
            
            if count < 8
                bluePixelLastBit = imgBluePixel(1,end);
                tempCharBin(1,count+1) = bluePixelLastBit;
                count = count + 1;
                
                if count == 8
                    count = 0;
                    if tempCharBin == getDelimiter()
                        tempCharBin = '';
                        innerLoopBroken = true;
                        break;
                    else
                        charInt = bin2dec(tempCharBin);
                        encodedMessage(1, charCount) = char(charInt);
                        charCount = charCount + 1;
                        tempCharBin = '';
                    end
                end
            end
        end
        
        if innerLoopBroken == true
            break;
        end
    end
    
    endDateTime = datetime('now', 'Format', 'dd-MM-yyyy HH:mm:ss.SSS');
    td = endDateTime - startDateTime;
    td.Format= 'hh:mm:ss.SSSSS';
    td = milliseconds(td);
    td = num2str(td);
    fprintf('\nDecoding Time: %s milliseconds\n\n', td);
end