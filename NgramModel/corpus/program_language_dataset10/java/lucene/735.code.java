package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class NorwegianStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "a", -1, 1, "", this),
            new Among ( "e", -1, 1, "", this),
            new Among ( "ede", 1, 1, "", this),
            new Among ( "ande", 1, 1, "", this),
            new Among ( "ende", 1, 1, "", this),
            new Among ( "ane", 1, 1, "", this),
            new Among ( "ene", 1, 1, "", this),
            new Among ( "hetene", 6, 1, "", this),
            new Among ( "erte", 1, 3, "", this),
            new Among ( "en", -1, 1, "", this),
            new Among ( "heten", 9, 1, "", this),
            new Among ( "ar", -1, 1, "", this),
            new Among ( "er", -1, 1, "", this),
            new Among ( "heter", 12, 1, "", this),
            new Among ( "s", -1, 2, "", this),
            new Among ( "as", 14, 1, "", this),
            new Among ( "es", 14, 1, "", this),
            new Among ( "edes", 16, 1, "", this),
            new Among ( "endes", 16, 1, "", this),
            new Among ( "enes", 16, 1, "", this),
            new Among ( "hetenes", 19, 1, "", this),
            new Among ( "ens", 14, 1, "", this),
            new Among ( "hetens", 21, 1, "", this),
            new Among ( "ers", 14, 1, "", this),
            new Among ( "ets", 14, 1, "", this),
            new Among ( "et", -1, 1, "", this),
            new Among ( "het", 25, 1, "", this),
            new Among ( "ert", -1, 3, "", this),
            new Among ( "ast", -1, 1, "", this)
        };
        private Among a_1[] = {
            new Among ( "dt", -1, -1, "", this),
            new Among ( "vt", -1, -1, "", this)
        };
        private Among a_2[] = {
            new Among ( "leg", -1, 1, "", this),
            new Among ( "eleg", 0, 1, "", this),
            new Among ( "ig", -1, 1, "", this),
            new Among ( "eig", 2, 1, "", this),
            new Among ( "lig", 2, 1, "", this),
            new Among ( "elig", 4, 1, "", this),
            new Among ( "els", -1, 1, "", this),
            new Among ( "lov", -1, 1, "", this),
            new Among ( "elov", 7, 1, "", this),
            new Among ( "slov", 7, 1, "", this),
            new Among ( "hetslov", 9, 1, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 48, 0, 128 };
        private static final char g_s_ending[] = {119, 125, 149, 1 };
        private int I_x;
        private int I_p1;
        private void copy_from(NorwegianStemmer other) {
            I_x = other.I_x;
            I_p1 = other.I_p1;
            super.copy_from(other);
        }
        private boolean r_mark_regions() {
            int v_1;
            int v_2;
            I_p1 = limit;
            v_1 = cursor;
            {
                int c = cursor + 3;
                if (0 > c || c > limit)
                {
                    return false;
                }
                cursor = c;
            }
            I_x = cursor;
            cursor = v_1;
            golab0: while(true)
            {
                v_2 = cursor;
                lab1: do {
                    if (!(in_grouping(g_v, 97, 248)))
                    {
                        break lab1;
                    }
                    cursor = v_2;
                    break golab0;
                } while (false);
                cursor = v_2;
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            golab2: while(true)
            {
                lab3: do {
                    if (!(out_grouping(g_v, 97, 248)))
                    {
                        break lab3;
                    }
                    break golab2;
                } while (false);
                if (cursor >= limit)
                {
                    return false;
                }
                cursor++;
            }
            I_p1 = cursor;
            lab4: do {
                if (!(I_p1 < I_x))
                {
                    break lab4;
                }
                I_p1 = I_x;
            } while (false);
            return true;
        }
        private boolean r_main_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_0, 29);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            limit_backward = v_2;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_del();
                    break;
                case 2:
                    lab0: do {
                        v_3 = limit - cursor;
                        lab1: do {
                            if (!(in_grouping_b(g_s_ending, 98, 122)))
                            {
                                break lab1;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_3;
                        if (!(eq_s_b(1, "k")))
                        {
                            return false;
                        }
                        if (!(out_grouping_b(g_v, 97, 248)))
                        {
                            return false;
                        }
                    } while (false);
                    slice_del();
                    break;
                case 3:
                    slice_from("er");
                    break;
            }
            return true;
        }
        private boolean r_consonant_pair() {
            int v_1;
            int v_2;
            int v_3;
            v_1 = limit - cursor;
            v_2 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_3 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_2;
            ket = cursor;
            if (find_among_b(a_1, 2) == 0)
            {
                limit_backward = v_3;
                return false;
            }
            bra = cursor;
            limit_backward = v_3;
            cursor = limit - v_1;
            if (cursor <= limit_backward)
            {
                return false;
            }
            cursor--;
            bra = cursor;
            slice_del();
            return true;
        }
        private boolean r_other_suffix() {
            int among_var;
            int v_1;
            int v_2;
            v_1 = limit - cursor;
            if (cursor < I_p1)
            {
                return false;
            }
            cursor = I_p1;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_2, 11);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            limit_backward = v_2;
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_del();
                    break;
            }
            return true;
        }
        public boolean stem() {
            int v_1;
            int v_2;
            int v_3;
            int v_4;
            v_1 = cursor;
            lab0: do {
                if (!r_mark_regions())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            limit_backward = cursor; cursor = limit;
            v_2 = limit - cursor;
            lab1: do {
                if (!r_main_suffix())
                {
                    break lab1;
                }
            } while (false);
            cursor = limit - v_2;
            v_3 = limit - cursor;
            lab2: do {
                if (!r_consonant_pair())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            v_4 = limit - cursor;
            lab3: do {
                if (!r_other_suffix())
                {
                    break lab3;
                }
            } while (false);
            cursor = limit - v_4;
            cursor = limit_backward;            return true;
        }
}
