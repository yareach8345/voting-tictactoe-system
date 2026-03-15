drop table if exists player;
drop table if exists tictactoe_game;
drop table if exists game_record;
drop table if exists game_group_info;

create table if not exists game_group_info(
    id int primary key auto_increment,
    group_id varchar(255) not null unique,
    game_type enum('NORMAL','INFINITY') not null default 'NORMAL',
    state enum('GENERATED', 'RECRUITING', 'BEFORE_START', 'PLAYING', 'FINISHED') not null default 'GENERATED',
    last_updated timestamp default current_timestamp on update current_timestamp,

    unique index group_idx(group_id)
);

/**
    group_user_idx
    - 特定のグループで同一なＩＤのユーザーが重複登録されないように制限
        - そのゆえ、user_idはユーザーごとに唯一な値であること
    - 特定のグループの特定なチームに関する作業を加速化
 */
create table if not exists player(
    id int primary key auto_increment,
    group_id varchar(255) not null,
    user_id varchar(255) not null,
    team enum('O', 'X') default null,

    foreign key(group_id) references game_group_info(group_id) on delete cascade,
    unique index group_user_idx(group_id, user_id)
);

create table if not exists tictactoe_game(
    id int primary key auto_increment,
    group_id varchar(255) not null unique,
    type enum('NORMAL','INFINITY') not null default 'NORMAL',
    state enum('X_WON', 'O_WON', 'DRAW', 'IN_PROGRESS') not null default 'IN_PROGRESS',

    foreign key(group_id) references game_group_info(group_id) on delete cascade,
    unique index group_idx(group_id)
);

create table if not exists game_record(
    id int primary key auto_increment,
    group_id varchar(255) not null,
    team enum('O', 'X') not null,
    x int not null check ( x >= 0 and x <= 2 ),
    y int not null check ( y >= 0 and y <= 2 ),

    foreign key(group_id) references game_group_info(group_id) on delete cascade,
    index group_idx(group_id)
);