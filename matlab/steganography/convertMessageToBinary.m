%% 
% .
% Convert the Message into Binary
% The function takes a string message and returns a vector of binary strings 
% of each character.

function [binaryMsgList, dispStr] = convertMessageToBinary(msg)
    disp(['Message: ' msg]);
    disp('Binary Representation: ');
    dispStr = '';
    dispStr = horzcat(dispStr, sprintf('%s\n', ['Message: ' msg]));
    dispStr = horzcat(dispStr, sprintf('%s\n', 'Binary Representation: '));
    
    msgAsNums = double(msg);
    binaryMsgList = dec2bin(msgAsNums);
    [rSize, ~] = size(binaryMsgList); %Get number of rows and cols in binaryMsgList
    binaryMsgList = padStringList(binaryMsgList, '0', 8, 0);
    
    %Display each char from msg and its binary represenation
    for x = 1:rSize
        disp([msg(x) '| ' num2str(binaryMsgList(x, :))]);
        dispStr = horzcat(dispStr, sprintf('%s\n', [msg(x) '| ' num2str(binaryMsgList(x, :))]));
    end
    
    fprintf('\n');
    dispStr = horzcat(dispStr, sprintf('\n'));
end