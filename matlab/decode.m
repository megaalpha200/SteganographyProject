% Decode Function
% The function takes a asks the user to enter the location of the image they 
% want to decode. Finally, the function displays the decoded message.

function decode()
    imgPath = input('Enter the location of the encoded image: ', 's');
    
    %Read the image and retrieved the encoded message
    img = imread(imgPath);
    msg = retrieveEncodedMessageFromImage(img);
    
    %Display the retrieved message
    disp(['The message is "' msg '"']);
end