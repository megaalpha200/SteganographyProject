% Encode Function
% The function takes a asks the user to enter a string message, the location 
% of the image they want to encode (as a string), and the location of the new 
% encoded image (as a string). Finally, the function writes the new image into 
% a file.

function encode()
    msg = input('Enter a message: ', 's');
    imgPath = input('Enter the location of the image: ', 's');
    newImgPath = input('Enter the location of the encoded image: ', 's');
    
    %Read the image and produce an new encoded version
    img = imread(imgPath);
    newImg = embedMessage(img, msg);
    
    %Write the image to a file
    imwrite(newImg, [newImgPath '.png']);
    movefile([newImgPath '.png'], newImgPath);
end