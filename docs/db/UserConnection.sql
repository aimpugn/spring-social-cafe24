-- 이 소스 코드에서는 userId, providerUserId 모두 mallId로 저장된다.
create table UserConnection (
 userId varchar(255) not null,
 providerId varchar(255) not null,
 providerUserId varchar(255),
 rank int not null,
 displayName varchar(255),
 profileUrl varchar(512),
 imageUrl varchar(512),
 accessToken varchar(1024) not null,
 secret varchar(255),
 refreshToken varchar(255),
 expireTime bigint,
 primary key (providerId, userId));