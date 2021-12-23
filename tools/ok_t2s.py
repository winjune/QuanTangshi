import regex
import json


def fun():
    temp_s = set()

    t2s = dict()
    multi_s = set()

    re_split = regex.compile(r'\s+')
    count = 0
    with open('TSCharacters.txt', encoding='utf-8') as f:
        all_lines = f.read().strip().split('\n')

    # 额外补充
    # 一些通假字、异体字，基本是无歧义转换
    extra = ('慙 惭',
             '羗 羌',
             '歛 敛',  # 此条有丢失意义的可能
             '遶 绕',
             '疎 疏',
             '嬾 懒',
             '鴈 雁',
             '劒 剑',
             '幷 并',
             '帬 裙',
             '猨 猿',
             '桮 杯',
             '堦 阶',
             '緜 绵',
             '僊 仙',
             '鬬 斗',
             '徴 徵',
             '逈 迥',
             '蔕 蒂',
             '呌 叫',
             '駞 驼',
             '隟 隙',
             '鵶 鸦',
             '賸 剩',
             '竚 伫',
             '崪 崒',
             '燄 焰',
             '汚 污',
             '栢 柏',
             '廻 回',
             '澂 澄',
             '濬 浚',
             '臯 皋',
             '虵 蛇',
             )
    all_lines.extend(extra)
        
                                        
    for line in all_lines:
        line = line.strip()
        if not line:
            continue
        
        t = re_split.split(line)
        assert t[0] not in t2s, '出现重复的繁体字 %s' % t[0]

        # 特例
        if t[0] == '餘':
            assert t == ['餘', '余', '馀']
            t = ('餘', '馀')
            
        elif t[0] == '鉅':
            assert t == ['鉅', '巨', '钜']
            t = ('鉅', '钜')
            
        elif t[0] == '瀰':
            assert t == ['瀰', '弥', '㳽']
            t = ('瀰', '㳽')
            
        elif t[0] == '閤':
            assert t == ['閤', '合']
            continue

        # 只保留BMP字符
        if ord(t[0]) > 0xffff or \
                all(True if ord(i) > 0xffff else False for i in t[1:]):
            continue

        # 一样的情况
        # if t[0] == t[1]:
        #    continue

        # 保存 繁简映射
        assert ord(t[1]) <= 0xffff, '出现Non-BMP的简体字'
        t2s[t[0]] = t[1]

        # 保存 多繁对一简
        if t[1] in temp_s:
            multi_s.add(t[1])
        else:
            temp_s.add(t[1])

        # 打印 多简的情况
        # if len(t) > 2:
        #    print(t, [1 if ord(ch)>0xffff else 0 for ch in t])

    # print(multi_s)
    print('繁->简映射：', len(t2s))
    print('多繁对一简：', len(multi_s))
    return t2s, multi_s


t2s, multi_s = fun()
t2s = {ord(k): ord(v) for k, v in t2s.items()}
multi_s = [ord(i) for i in multi_s]

# 排序
l = sorted(t2s.items(), key=lambda i: i[0])
k_list = []
v_list = []
for k, v in l:
    k_list.append(k)
    v_list.append(v)
assert len(k_list) == len(v_list)

multi_s.sort()

# java code
def line(s):
    s = regex.sub(r'([^\n]{0,60}(?:\};|,))', r'\1\n', s)
    return s

with open('java.txt', 'w') as f:

    s = "public final static int[] key = {\n" + \
    ','.join(str(i) for i in k_list) + '};\n'
    s = line(s)
    f.write(s)

    s = "public final static int[] value = {\n" + \
        ','.join(str(i) for i in v_list) + '};\n'
    s = line(s)
    f.write(s)

    s = "public final static int[] multi_s = {\n" + \
        ','.join(str(i) for i in multi_s) + '};'
    s = line(s)
    f.write(s)
    

##with open('map.json', 'w') as f:
##    json.dump([k_list, v_list, multi_s], f, separators=(',', ':'))
