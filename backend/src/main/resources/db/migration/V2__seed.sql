-- Conquistas iniciais
INSERT INTO achievements (code, name, description, icon, xp_reward) VALUES
 ('FIRST_REVIEW',  'Crítico Estreante',  'Publicou sua primeira avaliação',        'star',   50),
 ('REVIEWS_10',    'Crítico de Plantão', 'Publicou 10 avaliações',                 'stars',  150),
 ('STREAK_7',      'Semana em Chamas',   '7 dias seguidos de atividade',           'fire',   100),
 ('STREAK_30',     'Mês Lendário',       '30 dias seguidos de atividade',          'trophy', 500),
 ('FIRST_LIST',    'Curador',            'Criou sua primeira watchlist',           'list',   30),
 ('MARATHON_10',   'Maratonista',        'Marcou 10 títulos como assistidos',      'film',   120),
 ('LEVEL_5',       'Veterano',           'Alcançou o nível 5',                     'shield', 200);

-- Missões diárias/semanais
INSERT INTO missions (code, name, cadence, target, xp_reward, active) VALUES
 ('RATE_1',        'Avalie 1 título hoje',              'DAILY',  1, 15, true),
 ('ADD_WATCHLIST', 'Adicione 1 título a uma lista',     'DAILY',  1, 10, true),
 ('MARK_WATCHED',  'Marque 1 título como assistido',    'DAILY',  1, 10, true),
 ('RATE_5_WEEK',   'Avalie 5 títulos nesta semana',     'WEEKLY', 5, 60, true);
