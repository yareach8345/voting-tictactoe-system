drop table if exists player;
drop table if exists game_record;
drop table if exists tictactoe_game_info;
drop table if exists game_group_info;

create table if not exists game_group_info(
    id int primary key auto_increment,
    group_id varchar(255) not null unique,
    game_type varchar(10) not null default 'NORMAL' check(game_type in ('NORMAL','INFINITY')),
    state varchar(20) not null default 'GENERATED' check(state in ('GENERATED', 'RECRUITING', 'BEFORE_START', 'PLAYING', 'FINISHED')),
    last_updated timestamp default current_timestamp on update current_timestamp
);

create unique index game_group_info_group_idx on game_group_info(group_id);

/**
    player_group_user_idx
    - 特定のグループで同一なＩＤのユーザーが重複登録されないように制限
        - そのゆえ、user_idはユーザーごとに唯一な値であること
    - 特定のグループの特定なチームに関する作業を加速化
 */
create table if not exists player(
    id int primary key auto_increment,
    group_id varchar(255) not null,
    user_id varchar(255) not null,
    team VARCHAR(1) check (team in ('O', 'X')),

    foreign key(group_id) references game_group_info(group_id) on delete cascade
);

create unique index player_group_user_idx on player(group_id, user_id);

create table if not exists tictactoe_game_info(
    id int primary key auto_increment,
    group_id varchar(255) not null unique,
    type VARCHAR(8) not null default 'NORMAL' check (type in ('NORMAL', 'INFINITY')),

    foreign key(group_id) references game_group_info(group_id) on delete cascade
);

create unique index tictactoe_game_info_group_index on tictactoe_game_info(group_id);

create table if not exists game_record(
    id int primary key auto_increment,
    group_id varchar(255) not null,
    team VARCHAR(1) check (team in ('O', 'X')),
    x int not null check ( x >= 0 and x <= 2 ),
    y int not null check ( y >= 0 and y <= 2 ),
    timestamp datetime not null default current_timestamp,

    foreign key(group_id) references game_group_info(group_id) on delete cascade
);

create index game_record_group_index on game_record(group_id);