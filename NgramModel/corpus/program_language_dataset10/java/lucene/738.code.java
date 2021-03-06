package org.tartarus.snowball.ext;
import org.tartarus.snowball.SnowballProgram;
import org.tartarus.snowball.Among;
public class RomanianStemmer extends SnowballProgram {
        private Among a_0[] = {
            new Among ( "", -1, 3, "", this),
            new Among ( "I", 0, 1, "", this),
            new Among ( "U", 0, 2, "", this)
        };
        private Among a_1[] = {
            new Among ( "ea", -1, 3, "", this),
            new Among ( "a\u0163ia", -1, 7, "", this),
            new Among ( "aua", -1, 2, "", this),
            new Among ( "iua", -1, 4, "", this),
            new Among ( "a\u0163ie", -1, 7, "", this),
            new Among ( "ele", -1, 3, "", this),
            new Among ( "ile", -1, 5, "", this),
            new Among ( "iile", 6, 4, "", this),
            new Among ( "iei", -1, 4, "", this),
            new Among ( "atei", -1, 6, "", this),
            new Among ( "ii", -1, 4, "", this),
            new Among ( "ului", -1, 1, "", this),
            new Among ( "ul", -1, 1, "", this),
            new Among ( "elor", -1, 3, "", this),
            new Among ( "ilor", -1, 4, "", this),
            new Among ( "iilor", 14, 4, "", this)
        };
        private Among a_2[] = {
            new Among ( "icala", -1, 4, "", this),
            new Among ( "iciva", -1, 4, "", this),
            new Among ( "ativa", -1, 5, "", this),
            new Among ( "itiva", -1, 6, "", this),
            new Among ( "icale", -1, 4, "", this),
            new Among ( "a\u0163iune", -1, 5, "", this),
            new Among ( "i\u0163iune", -1, 6, "", this),
            new Among ( "atoare", -1, 5, "", this),
            new Among ( "itoare", -1, 6, "", this),
            new Among ( "\u0103toare", -1, 5, "", this),
            new Among ( "icitate", -1, 4, "", this),
            new Among ( "abilitate", -1, 1, "", this),
            new Among ( "ibilitate", -1, 2, "", this),
            new Among ( "ivitate", -1, 3, "", this),
            new Among ( "icive", -1, 4, "", this),
            new Among ( "ative", -1, 5, "", this),
            new Among ( "itive", -1, 6, "", this),
            new Among ( "icali", -1, 4, "", this),
            new Among ( "atori", -1, 5, "", this),
            new Among ( "icatori", 18, 4, "", this),
            new Among ( "itori", -1, 6, "", this),
            new Among ( "\u0103tori", -1, 5, "", this),
            new Among ( "icitati", -1, 4, "", this),
            new Among ( "abilitati", -1, 1, "", this),
            new Among ( "ivitati", -1, 3, "", this),
            new Among ( "icivi", -1, 4, "", this),
            new Among ( "ativi", -1, 5, "", this),
            new Among ( "itivi", -1, 6, "", this),
            new Among ( "icit\u0103i", -1, 4, "", this),
            new Among ( "abilit\u0103i", -1, 1, "", this),
            new Among ( "ivit\u0103i", -1, 3, "", this),
            new Among ( "icit\u0103\u0163i", -1, 4, "", this),
            new Among ( "abilit\u0103\u0163i", -1, 1, "", this),
            new Among ( "ivit\u0103\u0163i", -1, 3, "", this),
            new Among ( "ical", -1, 4, "", this),
            new Among ( "ator", -1, 5, "", this),
            new Among ( "icator", 35, 4, "", this),
            new Among ( "itor", -1, 6, "", this),
            new Among ( "\u0103tor", -1, 5, "", this),
            new Among ( "iciv", -1, 4, "", this),
            new Among ( "ativ", -1, 5, "", this),
            new Among ( "itiv", -1, 6, "", this),
            new Among ( "ical\u0103", -1, 4, "", this),
            new Among ( "iciv\u0103", -1, 4, "", this),
            new Among ( "ativ\u0103", -1, 5, "", this),
            new Among ( "itiv\u0103", -1, 6, "", this)
        };
        private Among a_3[] = {
            new Among ( "ica", -1, 1, "", this),
            new Among ( "abila", -1, 1, "", this),
            new Among ( "ibila", -1, 1, "", this),
            new Among ( "oasa", -1, 1, "", this),
            new Among ( "ata", -1, 1, "", this),
            new Among ( "ita", -1, 1, "", this),
            new Among ( "anta", -1, 1, "", this),
            new Among ( "ista", -1, 3, "", this),
            new Among ( "uta", -1, 1, "", this),
            new Among ( "iva", -1, 1, "", this),
            new Among ( "ic", -1, 1, "", this),
            new Among ( "ice", -1, 1, "", this),
            new Among ( "abile", -1, 1, "", this),
            new Among ( "ibile", -1, 1, "", this),
            new Among ( "isme", -1, 3, "", this),
            new Among ( "iune", -1, 2, "", this),
            new Among ( "oase", -1, 1, "", this),
            new Among ( "ate", -1, 1, "", this),
            new Among ( "itate", 17, 1, "", this),
            new Among ( "ite", -1, 1, "", this),
            new Among ( "ante", -1, 1, "", this),
            new Among ( "iste", -1, 3, "", this),
            new Among ( "ute", -1, 1, "", this),
            new Among ( "ive", -1, 1, "", this),
            new Among ( "ici", -1, 1, "", this),
            new Among ( "abili", -1, 1, "", this),
            new Among ( "ibili", -1, 1, "", this),
            new Among ( "iuni", -1, 2, "", this),
            new Among ( "atori", -1, 1, "", this),
            new Among ( "osi", -1, 1, "", this),
            new Among ( "ati", -1, 1, "", this),
            new Among ( "itati", 30, 1, "", this),
            new Among ( "iti", -1, 1, "", this),
            new Among ( "anti", -1, 1, "", this),
            new Among ( "isti", -1, 3, "", this),
            new Among ( "uti", -1, 1, "", this),
            new Among ( "i\u015Fti", -1, 3, "", this),
            new Among ( "ivi", -1, 1, "", this),
            new Among ( "it\u0103i", -1, 1, "", this),
            new Among ( "o\u015Fi", -1, 1, "", this),
            new Among ( "it\u0103\u0163i", -1, 1, "", this),
            new Among ( "abil", -1, 1, "", this),
            new Among ( "ibil", -1, 1, "", this),
            new Among ( "ism", -1, 3, "", this),
            new Among ( "ator", -1, 1, "", this),
            new Among ( "os", -1, 1, "", this),
            new Among ( "at", -1, 1, "", this),
            new Among ( "it", -1, 1, "", this),
            new Among ( "ant", -1, 1, "", this),
            new Among ( "ist", -1, 3, "", this),
            new Among ( "ut", -1, 1, "", this),
            new Among ( "iv", -1, 1, "", this),
            new Among ( "ic\u0103", -1, 1, "", this),
            new Among ( "abil\u0103", -1, 1, "", this),
            new Among ( "ibil\u0103", -1, 1, "", this),
            new Among ( "oas\u0103", -1, 1, "", this),
            new Among ( "at\u0103", -1, 1, "", this),
            new Among ( "it\u0103", -1, 1, "", this),
            new Among ( "ant\u0103", -1, 1, "", this),
            new Among ( "ist\u0103", -1, 3, "", this),
            new Among ( "ut\u0103", -1, 1, "", this),
            new Among ( "iv\u0103", -1, 1, "", this)
        };
        private Among a_4[] = {
            new Among ( "ea", -1, 1, "", this),
            new Among ( "ia", -1, 1, "", this),
            new Among ( "esc", -1, 1, "", this),
            new Among ( "\u0103sc", -1, 1, "", this),
            new Among ( "ind", -1, 1, "", this),
            new Among ( "\u00E2nd", -1, 1, "", this),
            new Among ( "are", -1, 1, "", this),
            new Among ( "ere", -1, 1, "", this),
            new Among ( "ire", -1, 1, "", this),
            new Among ( "\u00E2re", -1, 1, "", this),
            new Among ( "se", -1, 2, "", this),
            new Among ( "ase", 10, 1, "", this),
            new Among ( "sese", 10, 2, "", this),
            new Among ( "ise", 10, 1, "", this),
            new Among ( "use", 10, 1, "", this),
            new Among ( "\u00E2se", 10, 1, "", this),
            new Among ( "e\u015Fte", -1, 1, "", this),
            new Among ( "\u0103\u015Fte", -1, 1, "", this),
            new Among ( "eze", -1, 1, "", this),
            new Among ( "ai", -1, 1, "", this),
            new Among ( "eai", 19, 1, "", this),
            new Among ( "iai", 19, 1, "", this),
            new Among ( "sei", -1, 2, "", this),
            new Among ( "e\u015Fti", -1, 1, "", this),
            new Among ( "\u0103\u015Fti", -1, 1, "", this),
            new Among ( "ui", -1, 1, "", this),
            new Among ( "ezi", -1, 1, "", this),
            new Among ( "\u00E2i", -1, 1, "", this),
            new Among ( "a\u015Fi", -1, 1, "", this),
            new Among ( "se\u015Fi", -1, 2, "", this),
            new Among ( "ase\u015Fi", 29, 1, "", this),
            new Among ( "sese\u015Fi", 29, 2, "", this),
            new Among ( "ise\u015Fi", 29, 1, "", this),
            new Among ( "use\u015Fi", 29, 1, "", this),
            new Among ( "\u00E2se\u015Fi", 29, 1, "", this),
            new Among ( "i\u015Fi", -1, 1, "", this),
            new Among ( "u\u015Fi", -1, 1, "", this),
            new Among ( "\u00E2\u015Fi", -1, 1, "", this),
            new Among ( "a\u0163i", -1, 2, "", this),
            new Among ( "ea\u0163i", 38, 1, "", this),
            new Among ( "ia\u0163i", 38, 1, "", this),
            new Among ( "e\u0163i", -1, 2, "", this),
            new Among ( "i\u0163i", -1, 2, "", this),
            new Among ( "\u00E2\u0163i", -1, 2, "", this),
            new Among ( "ar\u0103\u0163i", -1, 1, "", this),
            new Among ( "ser\u0103\u0163i", -1, 2, "", this),
            new Among ( "aser\u0103\u0163i", 45, 1, "", this),
            new Among ( "seser\u0103\u0163i", 45, 2, "", this),
            new Among ( "iser\u0103\u0163i", 45, 1, "", this),
            new Among ( "user\u0103\u0163i", 45, 1, "", this),
            new Among ( "\u00E2ser\u0103\u0163i", 45, 1, "", this),
            new Among ( "ir\u0103\u0163i", -1, 1, "", this),
            new Among ( "ur\u0103\u0163i", -1, 1, "", this),
            new Among ( "\u00E2r\u0103\u0163i", -1, 1, "", this),
            new Among ( "am", -1, 1, "", this),
            new Among ( "eam", 54, 1, "", this),
            new Among ( "iam", 54, 1, "", this),
            new Among ( "em", -1, 2, "", this),
            new Among ( "asem", 57, 1, "", this),
            new Among ( "sesem", 57, 2, "", this),
            new Among ( "isem", 57, 1, "", this),
            new Among ( "usem", 57, 1, "", this),
            new Among ( "\u00E2sem", 57, 1, "", this),
            new Among ( "im", -1, 2, "", this),
            new Among ( "\u00E2m", -1, 2, "", this),
            new Among ( "\u0103m", -1, 2, "", this),
            new Among ( "ar\u0103m", 65, 1, "", this),
            new Among ( "ser\u0103m", 65, 2, "", this),
            new Among ( "aser\u0103m", 67, 1, "", this),
            new Among ( "seser\u0103m", 67, 2, "", this),
            new Among ( "iser\u0103m", 67, 1, "", this),
            new Among ( "user\u0103m", 67, 1, "", this),
            new Among ( "\u00E2ser\u0103m", 67, 1, "", this),
            new Among ( "ir\u0103m", 65, 1, "", this),
            new Among ( "ur\u0103m", 65, 1, "", this),
            new Among ( "\u00E2r\u0103m", 65, 1, "", this),
            new Among ( "au", -1, 1, "", this),
            new Among ( "eau", 76, 1, "", this),
            new Among ( "iau", 76, 1, "", this),
            new Among ( "indu", -1, 1, "", this),
            new Among ( "\u00E2ndu", -1, 1, "", this),
            new Among ( "ez", -1, 1, "", this),
            new Among ( "easc\u0103", -1, 1, "", this),
            new Among ( "ar\u0103", -1, 1, "", this),
            new Among ( "ser\u0103", -1, 2, "", this),
            new Among ( "aser\u0103", 84, 1, "", this),
            new Among ( "seser\u0103", 84, 2, "", this),
            new Among ( "iser\u0103", 84, 1, "", this),
            new Among ( "user\u0103", 84, 1, "", this),
            new Among ( "\u00E2ser\u0103", 84, 1, "", this),
            new Among ( "ir\u0103", -1, 1, "", this),
            new Among ( "ur\u0103", -1, 1, "", this),
            new Among ( "\u00E2r\u0103", -1, 1, "", this),
            new Among ( "eaz\u0103", -1, 1, "", this)
        };
        private Among a_5[] = {
            new Among ( "a", -1, 1, "", this),
            new Among ( "e", -1, 1, "", this),
            new Among ( "ie", 1, 1, "", this),
            new Among ( "i", -1, 1, "", this),
            new Among ( "\u0103", -1, 1, "", this)
        };
        private static final char g_v[] = {17, 65, 16, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 32, 0, 0, 4 };
        private boolean B_standard_suffix_removed;
        private int I_p2;
        private int I_p1;
        private int I_pV;
        private void copy_from(RomanianStemmer other) {
            B_standard_suffix_removed = other.B_standard_suffix_removed;
            I_p2 = other.I_p2;
            I_p1 = other.I_p1;
            I_pV = other.I_pV;
            super.copy_from(other);
        }
        private boolean r_prelude() {
            int v_1;
            int v_2;
            int v_3;
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    golab2: while(true)
                    {
                        v_2 = cursor;
                        lab3: do {
                            if (!(in_grouping(g_v, 97, 259)))
                            {
                                break lab3;
                            }
                            bra = cursor;
                            lab4: do {
                                v_3 = cursor;
                                lab5: do {
                                    if (!(eq_s(1, "u")))
                                    {
                                        break lab5;
                                    }
                                    ket = cursor;
                                    if (!(in_grouping(g_v, 97, 259)))
                                    {
                                        break lab5;
                                    }
                                    slice_from("U");
                                    break lab4;
                                } while (false);
                                cursor = v_3;
                                if (!(eq_s(1, "i")))
                                {
                                    break lab3;
                                }
                                ket = cursor;
                                if (!(in_grouping(g_v, 97, 259)))
                                {
                                    break lab3;
                                }
                                slice_from("I");
                            } while (false);
                            cursor = v_2;
                            break golab2;
                        } while (false);
                        cursor = v_2;
                        if (cursor >= limit)
                        {
                            break lab1;
                        }
                        cursor++;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }
        private boolean r_mark_regions() {
            int v_1;
            int v_2;
            int v_3;
            int v_6;
            int v_8;
            I_pV = limit;
            I_p1 = limit;
            I_p2 = limit;
            v_1 = cursor;
            lab0: do {
                lab1: do {
                    v_2 = cursor;
                    lab2: do {
                        if (!(in_grouping(g_v, 97, 259)))
                        {
                            break lab2;
                        }
                        lab3: do {
                            v_3 = cursor;
                            lab4: do {
                                if (!(out_grouping(g_v, 97, 259)))
                                {
                                    break lab4;
                                }
                                golab5: while(true)
                                {
                                    lab6: do {
                                        if (!(in_grouping(g_v, 97, 259)))
                                        {
                                            break lab6;
                                        }
                                        break golab5;
                                    } while (false);
                                    if (cursor >= limit)
                                    {
                                        break lab4;
                                    }
                                    cursor++;
                                }
                                break lab3;
                            } while (false);
                            cursor = v_3;
                            if (!(in_grouping(g_v, 97, 259)))
                            {
                                break lab2;
                            }
                            golab7: while(true)
                            {
                                lab8: do {
                                    if (!(out_grouping(g_v, 97, 259)))
                                    {
                                        break lab8;
                                    }
                                    break golab7;
                                } while (false);
                                if (cursor >= limit)
                                {
                                    break lab2;
                                }
                                cursor++;
                            }
                        } while (false);
                        break lab1;
                    } while (false);
                    cursor = v_2;
                    if (!(out_grouping(g_v, 97, 259)))
                    {
                        break lab0;
                    }
                    lab9: do {
                        v_6 = cursor;
                        lab10: do {
                            if (!(out_grouping(g_v, 97, 259)))
                            {
                                break lab10;
                            }
                            golab11: while(true)
                            {
                                lab12: do {
                                    if (!(in_grouping(g_v, 97, 259)))
                                    {
                                        break lab12;
                                    }
                                    break golab11;
                                } while (false);
                                if (cursor >= limit)
                                {
                                    break lab10;
                                }
                                cursor++;
                            }
                            break lab9;
                        } while (false);
                        cursor = v_6;
                        if (!(in_grouping(g_v, 97, 259)))
                        {
                            break lab0;
                        }
                        if (cursor >= limit)
                        {
                            break lab0;
                        }
                        cursor++;
                    } while (false);
                } while (false);
                I_pV = cursor;
            } while (false);
            cursor = v_1;
            v_8 = cursor;
            lab13: do {
                golab14: while(true)
                {
                    lab15: do {
                        if (!(in_grouping(g_v, 97, 259)))
                        {
                            break lab15;
                        }
                        break golab14;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                golab16: while(true)
                {
                    lab17: do {
                        if (!(out_grouping(g_v, 97, 259)))
                        {
                            break lab17;
                        }
                        break golab16;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                I_p1 = cursor;
                golab18: while(true)
                {
                    lab19: do {
                        if (!(in_grouping(g_v, 97, 259)))
                        {
                            break lab19;
                        }
                        break golab18;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                golab20: while(true)
                {
                    lab21: do {
                        if (!(out_grouping(g_v, 97, 259)))
                        {
                            break lab21;
                        }
                        break golab20;
                    } while (false);
                    if (cursor >= limit)
                    {
                        break lab13;
                    }
                    cursor++;
                }
                I_p2 = cursor;
            } while (false);
            cursor = v_8;
            return true;
        }
        private boolean r_postlude() {
            int among_var;
            int v_1;
            replab0: while(true)
            {
                v_1 = cursor;
                lab1: do {
                    bra = cursor;
                    among_var = find_among(a_0, 3);
                    if (among_var == 0)
                    {
                        break lab1;
                    }
                    ket = cursor;
                    switch(among_var) {
                        case 0:
                            break lab1;
                        case 1:
                            slice_from("i");
                            break;
                        case 2:
                            slice_from("u");
                            break;
                        case 3:
                            if (cursor >= limit)
                            {
                                break lab1;
                            }
                            cursor++;
                            break;
                    }
                    continue replab0;
                } while (false);
                cursor = v_1;
                break replab0;
            }
            return true;
        }
        private boolean r_RV() {
            if (!(I_pV <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_R1() {
            if (!(I_p1 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_R2() {
            if (!(I_p2 <= cursor))
            {
                return false;
            }
            return true;
        }
        private boolean r_step_0() {
            int among_var;
            int v_1;
            ket = cursor;
            among_var = find_among_b(a_1, 16);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_del();
                    break;
                case 2:
                    slice_from("a");
                    break;
                case 3:
                    slice_from("e");
                    break;
                case 4:
                    slice_from("i");
                    break;
                case 5:
                    {
                        v_1 = limit - cursor;
                        lab0: do {
                            if (!(eq_s_b(2, "ab")))
                            {
                                break lab0;
                            }
                            return false;
                        } while (false);
                        cursor = limit - v_1;
                    }
                    slice_from("i");
                    break;
                case 6:
                    slice_from("at");
                    break;
                case 7:
                    slice_from("a\u0163i");
                    break;
            }
            return true;
        }
        private boolean r_combo_suffix() {
            int among_var;
            int v_1;
            v_1 = limit - cursor;
            ket = cursor;
            among_var = find_among_b(a_2, 46);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R1())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_from("abil");
                    break;
                case 2:
                    slice_from("ibil");
                    break;
                case 3:
                    slice_from("iv");
                    break;
                case 4:
                    slice_from("ic");
                    break;
                case 5:
                    slice_from("at");
                    break;
                case 6:
                    slice_from("it");
                    break;
            }
            B_standard_suffix_removed = true;
            cursor = limit - v_1;
            return true;
        }
        private boolean r_standard_suffix() {
            int among_var;
            int v_1;
            B_standard_suffix_removed = false;
            replab0: while(true)
            {
                v_1 = limit - cursor;
                lab1: do {
                    if (!r_combo_suffix())
                    {
                        break lab1;
                    }
                    continue replab0;
                } while (false);
                cursor = limit - v_1;
                break replab0;
            }
            ket = cursor;
            among_var = find_among_b(a_3, 62);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_R2())
            {
                return false;
            }
            switch(among_var) {
                case 0:
                    return false;
                case 1:
                    slice_del();
                    break;
                case 2:
                    if (!(eq_s_b(1, "\u0163")))
                    {
                        return false;
                    }
                    bra = cursor;
                    slice_from("t");
                    break;
                case 3:
                    slice_from("ist");
                    break;
            }
            B_standard_suffix_removed = true;
            return true;
        }
        private boolean r_verb_suffix() {
            int among_var;
            int v_1;
            int v_2;
            int v_3;
            v_1 = limit - cursor;
            if (cursor < I_pV)
            {
                return false;
            }
            cursor = I_pV;
            v_2 = limit_backward;
            limit_backward = cursor;
            cursor = limit - v_1;
            ket = cursor;
            among_var = find_among_b(a_4, 94);
            if (among_var == 0)
            {
                limit_backward = v_2;
                return false;
            }
            bra = cursor;
            switch(among_var) {
                case 0:
                    limit_backward = v_2;
                    return false;
                case 1:
                    lab0: do {
                        v_3 = limit - cursor;
                        lab1: do {
                            if (!(out_grouping_b(g_v, 97, 259)))
                            {
                                break lab1;
                            }
                            break lab0;
                        } while (false);
                        cursor = limit - v_3;
                        if (!(eq_s_b(1, "u")))
                        {
                            limit_backward = v_2;
                            return false;
                        }
                    } while (false);
                    slice_del();
                    break;
                case 2:
                    slice_del();
                    break;
            }
            limit_backward = v_2;
            return true;
        }
        private boolean r_vowel_suffix() {
            int among_var;
            ket = cursor;
            among_var = find_among_b(a_5, 5);
            if (among_var == 0)
            {
                return false;
            }
            bra = cursor;
            if (!r_RV())
            {
                return false;
            }
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
            int v_5;
            int v_6;
            int v_7;
            int v_8;
            v_1 = cursor;
            lab0: do {
                if (!r_prelude())
                {
                    break lab0;
                }
            } while (false);
            cursor = v_1;
            v_2 = cursor;
            lab1: do {
                if (!r_mark_regions())
                {
                    break lab1;
                }
            } while (false);
            cursor = v_2;
            limit_backward = cursor; cursor = limit;
            v_3 = limit - cursor;
            lab2: do {
                if (!r_step_0())
                {
                    break lab2;
                }
            } while (false);
            cursor = limit - v_3;
            v_4 = limit - cursor;
            lab3: do {
                if (!r_standard_suffix())
                {
                    break lab3;
                }
            } while (false);
            cursor = limit - v_4;
            v_5 = limit - cursor;
            lab4: do {
                lab5: do {
                    v_6 = limit - cursor;
                    lab6: do {
                        if (!(B_standard_suffix_removed))
                        {
                            break lab6;
                        }
                        break lab5;
                    } while (false);
                    cursor = limit - v_6;
                    if (!r_verb_suffix())
                    {
                        break lab4;
                    }
                } while (false);
            } while (false);
            cursor = limit - v_5;
            v_7 = limit - cursor;
            lab7: do {
                if (!r_vowel_suffix())
                {
                    break lab7;
                }
            } while (false);
            cursor = limit - v_7;
            cursor = limit_backward;            
            v_8 = cursor;
            lab8: do {
                if (!r_postlude())
                {
                    break lab8;
                }
            } while (false);
            cursor = v_8;
            return true;
        }
}
