drop table if exists player;
drop table if exists game_group_info;
-- drop table if exists tictactoe;
-- drop table if exists game_record;

create table if not exists game_group_info(
    id int primary key auto_increment,
    group_id varchar(255) not null unique,
    game_type varchar(10) not null default 'NORMAL' check(game_type in ('NORMAL','INFINITY'))
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

    foreign key(group_id) references game_group_info(group_id)
);

create unique index player_group_user_idx on player(group_id, user_id);
