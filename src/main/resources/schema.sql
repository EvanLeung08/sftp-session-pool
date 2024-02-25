CREATE TABLE SFTP_CONFIG (
    ID INT AUTO_INCREMENT PRIMARY KEY,
    HOST VARCHAR(255),
    PORT INT,
    USERNAME VARCHAR(255),
    PASSWORD VARCHAR(255),
    MAXSESSIONS INT,
    MAXCHANNELS INT
);
