drop table if exists player;
# drop table if exists tictactoe;
# drop table if exists game_record;

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
    unique index group_user_idx(group_id, user_id)
);